package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;

import java.util.List;

public record VeterinarianListResponse(
        List<VeterinarianResponse> veterinarians
) {
    public static VeterinarianListResponse from(List<Veterinarian> veterinarians) {
        List<VeterinarianResponse> responses = veterinarians.stream()
                .map(VeterinarianResponse::from)
                .toList();
        return new VeterinarianListResponse(responses);
    }
}
