package PetMoa.PetMoa.domain.payment.service;

import PetMoa.PetMoa.domain.payment.dto.PaymentConfirmRequest;
import PetMoa.PetMoa.domain.payment.dto.PaymentCreateRequest;
import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.entity.TaxiReservation;
import PetMoa.PetMoa.domain.reservation.service.ReservationQueryService;
import PetMoa.PetMoa.global.client.toss.TossPaymentsClient;
import PetMoa.PetMoa.global.client.toss.dto.TossPaymentResponse;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import PetMoa.PetMoa.global.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentQueryService paymentQueryService;
    private final ReservationQueryService reservationQueryService;
    private final TossPaymentsClient tossPaymentsClient;

    private static final int DEPOSIT_AMOUNT = 10000;

    /**
     * 결제 요청 생성
     * - orderId 발급
     * - Payment 엔티티 생성 (PENDING 상태)
     */
    public Payment createPayment(Long userId, PaymentCreateRequest request) {
        Reservation reservation = reservationQueryService.getReservationById(request.reservationId());
        validateReservationOwnership(userId, reservation);

        // 예약 상태 검증 - PENDING 상태에서만 결제 생성 가능
        if (!reservation.canConfirm()) {
            throw new PaymentException("INVALID_RESERVATION_STATUS",
                    "결제 대기 상태의 예약만 결제할 수 있습니다. 현재 상태: " + reservation.getStatus());
        }

        // 이미 결제가 존재하는지 확인
        Payment existingPayment = paymentRepository.findByReservationId(request.reservationId());
        if (existingPayment != null) {
            throw new PaymentException("PAYMENT_ALREADY_EXISTS", "이미 결제가 존재합니다.");
        }

        // 택시비 계산
        int taxiFare = calculateTaxiFare(reservation);

        // orderId 생성
        String orderId = generateOrderId();

        Payment payment = Payment.builder()
                .reservation(reservation)
                .orderId(orderId)
                .depositAmount(DEPOSIT_AMOUNT)
                .taxiFare(taxiFare)
                .method(request.method())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 요청 생성 - paymentId: {}, orderId: {}, totalAmount: {}",
                savedPayment.getId(), orderId, savedPayment.getTotalAmount());

        return savedPayment;
    }

    /**
     * 결제 승인 처리
     * - 토스페이먼츠 API 호출
     * - Payment 상태 업데이트
     * - 예약 확정
     */
    public Payment confirmPayment(Long userId, PaymentConfirmRequest request) {
        Payment payment = paymentQueryService.getPaymentByOrderIdInternal(request.orderId());

        // 소유권 검증
        validateReservationOwnership(userId, payment.getReservation());

        // 결제 상태 검증 - PENDING 상태에서만 승인 가능
        if (!payment.isPending()) {
            throw new PaymentException("INVALID_PAYMENT_STATUS",
                    "대기 중인 결제만 승인할 수 있습니다. 현재 상태: " + payment.getStatus());
        }

        // 예약 상태 검증 - PENDING 상태에서만 결제 승인 가능
        Reservation reservation = payment.getReservation();
        if (!reservation.canConfirm()) {
            throw new PaymentException("INVALID_RESERVATION_STATUS",
                    "결제 대기 상태의 예약만 확정할 수 있습니다. 현재 상태: " + reservation.getStatus());
        }

        // 금액 검증
        if (!payment.getTotalAmount().equals(request.amount())) {
            throw new PaymentException("AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다.");
        }

        // 토스페이먼츠 결제 승인 API 호출
        TossPaymentResponse tossResponse = tossPaymentsClient.confirmPayment(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        if (!tossResponse.isApproved()) {
            payment.fail();
            throw new PaymentException("PAYMENT_NOT_APPROVED", "결제가 승인되지 않았습니다.");
        }

        // Payment 상태 업데이트
        payment.approve(request.paymentKey());

        // 예약 확정
        reservation.confirm();

        log.info("결제 승인 완료 - paymentId: {}, paymentKey: {}", payment.getId(), request.paymentKey());

        return payment;
    }

    /**
     * 환불 처리
     * - 환불 정책 적용 (24시간 전 100%, 12시간 전 50%, 당일 0%)
     * - 토스페이먼츠 취소 API 호출
     * - Payment 상태 업데이트
     */
    public Payment refundPayment(Long userId, Long paymentId, String cancelReason) {
        Payment payment = paymentQueryService.getPaymentByIdInternal(paymentId);
        validateReservationOwnership(userId, payment.getReservation());

        if (!payment.canRefund()) {
            throw new PaymentException("REFUND_NOT_ALLOWED", "환불할 수 없는 상태입니다.");
        }

        // 환불 비율 계산
        int refundRate = calculateRefundRate(payment.getReservation());
        int refundAmount = payment.getTotalAmount() * refundRate / 100;

        log.info("환불 처리 시작 - paymentId: {}, refundRate: {}%, refundAmount: {}",
                paymentId, refundRate, refundAmount);

        if (refundAmount == 0) {
            // 환불 금액이 0인 경우 (당일 취소)
            payment.cancelWithNoRefund(cancelReason + " (당일 취소로 환불 불가)");
            log.info("당일 취소로 환불 불가 - paymentId: {}", paymentId);
            return payment;
        }

        // 토스페이먼츠 취소 API 호출
        if (refundAmount == payment.getTotalAmount().intValue()) {
            // 전액 환불
            tossPaymentsClient.cancelPayment(payment.getPaymentKey(), cancelReason);
            payment.cancel(cancelReason);
        } else {
            // 부분 환불
            tossPaymentsClient.cancelPaymentPartially(payment.getPaymentKey(), cancelReason, refundAmount);
            payment.partialCancel(refundAmount, cancelReason);
        }

        log.info("환불 처리 완료 - paymentId: {}, refundAmount: {}", paymentId, refundAmount);

        return payment;
    }

    /**
     * 예약 ID로 환불 처리
     */
    public Payment refundByReservationId(Long userId, Long reservationId, String cancelReason) {
        Payment payment = paymentQueryService.getPaymentByReservationIdInternal(reservationId);
        return refundPayment(userId, payment.getId(), cancelReason);
    }

    private int calculateTaxiFare(Reservation reservation) {
        int taxiFare = 0;
        for (TaxiReservation taxiReservation : reservation.getTaxiReservations()) {
            if (taxiReservation.getFare() != null) {
                taxiFare += taxiReservation.getFare();
            }
        }
        return taxiFare;
    }

    private int calculateRefundRate(Reservation reservation) {
        HospitalReservation hr = reservation.getHospitalReservation();
        if (hr == null) {
            return 0;
        }

        LocalDateTime reservationTime = hr.getTimeSlot().getDate()
                .atTime(hr.getTimeSlot().getStartTime());
        LocalDateTime now = LocalDateTime.now();

        long hoursUntilReservation = ChronoUnit.HOURS.between(now, reservationTime);

        if (hoursUntilReservation >= 24) {
            return 100;
        } else if (hoursUntilReservation >= 12) {
            return 50;
        } else {
            return 0;
        }
    }

    private String generateOrderId() {
        return "PETMOA_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }

    private void validateReservationOwnership(Long userId, Reservation reservation) {
        if (!reservation.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 예약의 소유자가 아닙니다.");
        }
    }
}
