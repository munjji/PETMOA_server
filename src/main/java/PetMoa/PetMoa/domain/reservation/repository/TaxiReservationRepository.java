package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.reservation.entity.TaxiReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxiReservationRepository extends JpaRepository<TaxiReservation, Long> {
}
