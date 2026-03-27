package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalReservationRepository extends JpaRepository<HospitalReservation, Long> {
}
