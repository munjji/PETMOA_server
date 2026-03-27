package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VeterinarianRepository extends JpaRepository<Veterinarian, Long> {
}
