package PetMoa.PetMoa.domain.pet.controller;

import PetMoa.PetMoa.domain.pet.dto.PetCreateRequest;
import PetMoa.PetMoa.domain.pet.dto.PetListResponse;
import PetMoa.PetMoa.domain.pet.dto.PetResponse;
import PetMoa.PetMoa.domain.pet.dto.PetUpdateRequest;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.service.PetCommandService;
import PetMoa.PetMoa.domain.pet.service.PetQueryService;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Pet", description = "반려동물 API")
@RestController
@RequestMapping("/api/v1/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetQueryService petQueryService;
    private final PetCommandService petCommandService;

    @Operation(summary = "내 반려동물 목록 조회", description = "현재 로그인한 사용자의 반려동물 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<PetListResponse> getMyPets(
            @RequestHeader("X-User-Id") Long userId) {
        List<Pet> pets = petQueryService.getPetsByOwnerId(userId);
        return ApiResponse.onSuccess(PetListResponse.from(pets));
    }

    @Operation(summary = "반려동물 등록", description = "새로운 반려동물을 등록합니다.")
    @PostMapping
    public ApiResponse<PetResponse> createPet(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody PetCreateRequest request) {
        Pet pet = petCommandService.createPet(userId, request);
        return ApiResponse.onSuccess(PetResponse.from(pet));
    }

    @Operation(summary = "반려동물 정보 수정", description = "반려동물 정보를 수정합니다.")
    @PatchMapping("/{petId}")
    public ApiResponse<PetResponse> updatePet(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long petId,
            @RequestBody PetUpdateRequest request) {
        Pet pet = petCommandService.updatePet(petId, request);
        return ApiResponse.onSuccess(PetResponse.from(pet));
    }

    @Operation(summary = "반려동물 삭제", description = "반려동물을 삭제합니다.")
    @DeleteMapping("/{petId}")
    public ApiResponse<Void> deletePet(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long petId) {
        petCommandService.deletePet(petId);
        return ApiResponse.onSuccess(null);
    }
}
