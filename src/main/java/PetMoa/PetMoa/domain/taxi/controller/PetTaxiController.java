package PetMoa.PetMoa.domain.taxi.controller;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.taxi.dto.TaxiAvailabilityResponse;
import PetMoa.PetMoa.domain.taxi.service.PetTaxiQueryService;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Tag(name = "PetTaxi", description = "펫택시 API")
@RestController
@RequestMapping("/api/v1/pet-taxis")
@RequiredArgsConstructor
public class PetTaxiController {

    private final PetTaxiQueryService petTaxiQueryService;

    @Operation(summary = "펫택시 이용 가능 여부 확인", description = "해당 조건으로 펫택시 이용이 가능한지 확인합니다.")
    @GetMapping("/check-availability")
    public ApiResponse<TaxiAvailabilityResponse> checkAvailability(
            @RequestParam PetSize petSize,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupTime,
            @RequestParam String pickupAddress) {
        TaxiAvailabilityResponse response = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);
        return ApiResponse.onSuccess(response);
    }
}
