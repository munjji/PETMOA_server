package PetMoa.PetMoa.domain.hospital.controller;

import PetMoa.PetMoa.domain.hospital.dto.*;
import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.service.HospitalQueryService;
import PetMoa.PetMoa.domain.hospital.service.TimeSlotQueryService;
import PetMoa.PetMoa.domain.hospital.service.VeterinarianQueryService;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.global.apiPayload.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Hospital", description = "병원 API")
@RestController
@RequestMapping("/api/v1/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalQueryService hospitalQueryService;
    private final VeterinarianQueryService veterinarianQueryService;
    private final TimeSlotQueryService timeSlotQueryService;

    @Operation(summary = "병원 목록 조회", description = "병원 목록을 조회합니다. 이름, 주소, 반려동물 종류로 검색할 수 있습니다.")
    @GetMapping
    public ApiResponse<HospitalListResponse> getHospitals(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) PetType petType,
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false, defaultValue = "5") Double radius) {

        List<Hospital> hospitals;

        if (name != null && !name.isBlank()) {
            hospitals = hospitalQueryService.searchByName(name);
        } else if (address != null && !address.isBlank()) {
            hospitals = hospitalQueryService.searchByAddress(address);
        } else if (petType != null) {
            hospitals = hospitalQueryService.searchByPetType(petType);
        } else if (lat != null && lng != null) {
            hospitals = hospitalQueryService.searchNearby(lat, lng, radius);
        } else {
            hospitals = hospitalQueryService.getAllHospitals();
        }

        return ApiResponse.onSuccess(HospitalListResponse.from(hospitals));
    }

    @Operation(summary = "병원 상세 조회", description = "병원 상세 정보와 수의사 목록을 조회합니다.")
    @GetMapping("/{hospitalId}")
    public ApiResponse<HospitalDetailResponse> getHospitalDetail(
            @PathVariable Long hospitalId) {
        Hospital hospital = hospitalQueryService.getHospitalById(hospitalId);
        List<Veterinarian> veterinarians = veterinarianQueryService.getVeterinariansByHospitalId(hospitalId);
        return ApiResponse.onSuccess(HospitalDetailResponse.from(hospital, veterinarians));
    }

    @Operation(summary = "수의사 목록 조회", description = "해당 병원의 수의사 목록을 조회합니다.")
    @GetMapping("/{hospitalId}/veterinarians")
    public ApiResponse<VeterinarianListResponse> getVeterinarians(
            @PathVariable Long hospitalId) {
        List<Veterinarian> veterinarians = veterinarianQueryService.getVeterinariansByHospitalId(hospitalId);
        return ApiResponse.onSuccess(VeterinarianListResponse.from(veterinarians));
    }

    @Operation(summary = "수의사 예약 가능 시간 조회", description = "특정 날짜에 해당 수의사의 예약 가능한 시간대를 조회합니다.")
    @GetMapping("/{hospitalId}/veterinarians/{vetId}/time-slots")
    public ApiResponse<TimeSlotListResponse> getTimeSlots(
            @PathVariable Long hospitalId,
            @PathVariable Long vetId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Veterinarian veterinarian = veterinarianQueryService.getVeterinarianById(vetId);
        List<TimeSlot> timeSlots = timeSlotQueryService.getTimeSlotsByVeterinarianAndDate(vetId, date);
        return ApiResponse.onSuccess(TimeSlotListResponse.from(date, veterinarian, timeSlots));
    }
}
