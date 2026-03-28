package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.pet.entity.PetType;

import java.util.Set;

public record HospitalResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Set<PetType> availablePetTypes,
        Double distance
) {
    public static HospitalResponse from(Hospital hospital) {
        return from(hospital, null);
    }

    public static HospitalResponse from(Hospital hospital, Double distance) {
        return new HospitalResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhoneNumber(),
                hospital.getLatitude(),
                hospital.getLongitude(),
                hospital.getAvailablePetTypes(),
                distance
        );
    }
}
