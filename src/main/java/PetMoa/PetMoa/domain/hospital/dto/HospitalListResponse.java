package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;

import java.util.List;

public record HospitalListResponse(
        List<HospitalResponse> hospitals
) {
    public static HospitalListResponse from(List<Hospital> hospitals) {
        List<HospitalResponse> responses = hospitals.stream()
                .map(HospitalResponse::from)
                .toList();
        return new HospitalListResponse(responses);
    }
}
