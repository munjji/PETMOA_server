package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {
}
