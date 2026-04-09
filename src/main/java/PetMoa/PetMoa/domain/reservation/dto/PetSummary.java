package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.pet.entity.Pet;

public record PetSummary(
        Long id,
        String name
) {
    public static PetSummary from(Pet pet) {
        return new PetSummary(pet.getId(), pet.getName());
    }
}
