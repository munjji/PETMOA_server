package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalRepository extends JpaRepository<Hospital, Long>, HospitalRepositoryCustom {
}
