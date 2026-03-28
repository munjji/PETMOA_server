package PetMoa.PetMoa.domain.pet.dto;

import PetMoa.PetMoa.domain.pet.entity.PetSize;

public record PetUpdateRequest(
        String name,
        PetSize size,
        Integer age,
        Double weight,
        String breed
) {
}
