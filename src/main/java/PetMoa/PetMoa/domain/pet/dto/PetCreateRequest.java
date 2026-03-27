package PetMoa.PetMoa.domain.pet.dto;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PetCreateRequest {

    private final String name;
    private final PetType type;
    private final PetSize size;
    private final Integer age;
    private final Double weight;
    private final String breed;
}
