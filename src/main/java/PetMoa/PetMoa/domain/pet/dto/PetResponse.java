package PetMoa.PetMoa.domain.pet.dto;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;

public record PetResponse(
        Long id,
        String name,
        PetType type,
        PetSize size,
        String breed,
        Integer age,
        Double weight
) {
    public static PetResponse from(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getSize(),
                pet.getBreed(),
                pet.getAge(),
                pet.getWeight()
        );
    }
}
