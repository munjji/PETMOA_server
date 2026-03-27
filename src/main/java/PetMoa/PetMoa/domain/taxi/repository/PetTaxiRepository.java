package PetMoa.PetMoa.domain.taxi.repository;

import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PetTaxiRepository extends JpaRepository<PetTaxi, Long> {
}
