package PetMoa.PetMoa.domain.reservation.service;

import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.global.lock.DistributedLockExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationCommandService reservationCommandService;
    private final DistributedLockExecutor distributedLockExecutor;

    private static final String TIMESLOT_LOCK_PREFIX = "lock:timeslot:";

    public Reservation createReservation(Long userId, ReservationCreateRequest request) {
        Long timeSlotId = request.hospitalReservation().timeSlotId();
        String lockKey = TIMESLOT_LOCK_PREFIX + timeSlotId;

        log.debug("예약 생성 시도 - userId: {}, timeSlotId: {}", userId, timeSlotId);

        return distributedLockExecutor.executeWithLock(lockKey, () -> {
            return reservationCommandService.createReservation(userId, request);
        });
    }

    public Reservation cancelReservation(Long userId, Long reservationId) {
        return reservationCommandService.cancelReservation(userId, reservationId);
    }

    public int calculateRefundRate(Reservation reservation) {
        return reservationCommandService.calculateRefundRate(reservation);
    }
}
