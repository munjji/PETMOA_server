package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.pet.entity.PetType;

import java.util.List;
import java.util.Set;

public record HospitalDetailResponse(
        Long id,
        String name,
        String address,
        String phoneNumber,
        Double latitude,
        Double longitude,
        Set<PetType> availablePetTypes,
        List<VeterinarianResponse> veterinarians
) {
    public static HospitalDetailResponse from(Hospital hospital, List<Veterinarian> veterinarians) {
        List<VeterinarianResponse> vetResponses = veterinarians.stream()
                .map(VeterinarianResponse::from)
                .toList();

        return new HospitalDetailResponse(
                hospital.getId(),
                hospital.getName(),
                hospital.getAddress(),
                hospital.getPhoneNumber(),
                hospital.getLatitude(),
                hospital.getLongitude(),
                hospital.getAvailablePetTypes(),
                vetResponses
        );
    }
}
