package PetMoa.PetMoa.domain.pet.dto;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;

public record PetCreateRequest(
        String name,
        PetType type,
        PetSize size,
        Integer age,
        Double weight,
        String breed
) {
}
