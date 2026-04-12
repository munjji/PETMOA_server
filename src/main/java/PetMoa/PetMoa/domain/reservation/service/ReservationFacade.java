package PetMoa.PetMoa.domain.reservation.service;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import PetMoa.PetMoa.domain.payment.service.PaymentService;
import PetMoa.PetMoa.domain.reservation.dto.CancellationResult;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.global.lock.DistributedLockExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
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

    @Transactional
    public CancellationResult cancelReservation(Long userId, Long reservationId) {
        Reservation reservation = reservationQueryService.getReservationByIdAndUserId(reservationId, userId);
        HospitalReservation hospitalReservation = reservation.getHospitalReservation();

        CancellationResult cancellationResult;

        if (hospitalReservation == null) {
            cancellationResult = reservationCommandService.cancelReservation(userId, reservationId);
        } else {
            Long timeSlotId = hospitalReservation.getTimeSlot().getId();
            String lockKey = TIMESLOT_LOCK_PREFIX + timeSlotId;

            log.debug("예약 취소 시도 - userId: {}, reservationId: {}, timeSlotId: {}", userId, reservationId, timeSlotId);

            cancellationResult = distributedLockExecutor.executeWithLock(lockKey, () -> {
                return reservationCommandService.cancelReservation(userId, reservationId);
            });
        }

        // 결제가 존재하면 환불 처리
        Integer refundAmount = processRefundIfPaymentExists(userId, reservationId, cancellationResult.refundRate());

        return CancellationResult.of(
                cancellationResult.reservation(),
                cancellationResult.refundRate(),
                refundAmount
        );
    }

    /**
     * 결제가 존재하고 환불 가능한 상태이면 환불 처리
     */
    private Integer processRefundIfPaymentExists(Long userId, Long reservationId, int refundRate) {
        Payment payment = paymentRepository.findByReservationId(reservationId);

        if (payment == null) {
            log.debug("결제 내역 없음 - reservationId: {}", reservationId);
            return null;
        }

        if (!payment.canRefund()) {
            log.debug("환불 불가 상태 - paymentId: {}, status: {}", payment.getId(), payment.getStatus());
            return null;
        }

        log.info("자동 환불 처리 시작 - reservationId: {}, refundRate: {}%", reservationId, refundRate);

        Payment refundedPayment = paymentService.refundPayment(userId, payment.getId(), "예약 취소로 인한 환불");

        log.info("자동 환불 처리 완료 - paymentId: {}, refundAmount: {}", payment.getId(), refundedPayment.getRefundAmount());

        return refundedPayment.getRefundAmount();
    }
}
