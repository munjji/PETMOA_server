package PetMoa.PetMoa.domain.pet.repository;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetRepository extends JpaRepository<Pet, Long> {
}
