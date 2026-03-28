package PetMoa.PetMoa.domain.pet.dto;

import PetMoa.PetMoa.domain.pet.entity.Pet;

import java.util.List;

public record PetListResponse(
        List<PetResponse> pets
) {
    public static PetListResponse from(List<Pet> pets) {
        List<PetResponse> petResponses = pets.stream()
                .map(PetResponse::from)
                .toList();
        return new PetListResponse(petResponses);
    }
}
