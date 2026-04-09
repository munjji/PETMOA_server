package PetMoa.PetMoa.domain.reservation.service;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.repository.ReservationRepository;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

    private final ReservationRepository reservationRepository;

    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationRepository.findByIdWithDetails(id);
        if (reservation == null) {
            throw new EntityNotFoundException("예약을 찾을 수 없습니다. id=" + id);
        }
        return reservation;
    }

    public Reservation getReservationByIdAndUserId(Long reservationId, Long userId) {
        Reservation reservation = getReservationById(reservationId);
        validateOwnership(userId, reservation);
        return reservation;
    }

    public List<Reservation> getReservationsByUserId(Long userId) {
        return reservationRepository.findByUserIdWithDetails(userId);
    }

    private void validateOwnership(Long userId, Reservation reservation) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 예약의 소유자가 아닙니다.");
        }
    }
}
