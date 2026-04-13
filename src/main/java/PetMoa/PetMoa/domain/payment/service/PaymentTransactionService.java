package PetMoa.PetMoa.domain.payment.service;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.global.exception.PaymentException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 결제 관련 DB 트랜잭션 처리 전담 서비스
 * 외부 API 호출 없이 순수 DB 작업만 수행
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final PaymentQueryService paymentQueryService;

    /**
     * 결제 승인 전 검증 (트랜잭션 1)
     */
    @Transactional(readOnly = true)
    public Payment validateForConfirm(String orderId, Integer expectedAmount) {
        Payment payment = paymentQueryService.getPaymentByOrderIdInternal(orderId);

        if (!payment.isPending()) {
            throw new PaymentException("INVALID_PAYMENT_STATUS",
                    "대기 중인 결제만 승인할 수 있습니다. 현재 상태: " + payment.getStatus());
        }

        Reservation reservation = payment.getReservation();
        if (!reservation.canConfirm()) {
            throw new PaymentException("INVALID_RESERVATION_STATUS",
                    "결제 대기 상태의 예약만 확정할 수 있습니다. 현재 상태: " + reservation.getStatus());
        }

        if (!payment.getTotalAmount().equals(expectedAmount)) {
            throw new PaymentException("AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다.");
        }

        return payment;
    }

    /**
     * 결제 승인 완료 처리 (트랜잭션 2)
     */
    @Transactional
    public Payment completeConfirm(String orderId, String paymentKey) {
        Payment payment = paymentQueryService.getPaymentByOrderIdInternal(orderId);

        // 이중 검증 (동시성 대비)
        if (!payment.isPending()) {
            throw new PaymentException("INVALID_PAYMENT_STATUS",
                    "대기 중인 결제만 승인할 수 있습니다. 현재 상태: " + payment.getStatus());
        }

        payment.approve(paymentKey);
        payment.getReservation().confirm();

        log.info("결제 승인 완료 - paymentId: {}, paymentKey: {}", payment.getId(), paymentKey);
        return payment;
    }

    /**
     * 결제 승인 실패 처리
     */
    @Transactional
    public Payment failConfirm(String orderId) {
        Payment payment = paymentQueryService.getPaymentByOrderIdInternal(orderId);
        payment.fail();
        log.info("결제 승인 실패 - paymentId: {}", payment.getId());
        return payment;
    }

    /**
     * 환불 전 검증 (트랜잭션 1)
     */
    @Transactional(readOnly = true)
    public Payment validateForRefund(Long paymentId) {
        Payment payment = paymentQueryService.getPaymentByIdInternal(paymentId);

        if (!payment.canRefund()) {
            throw new PaymentException("REFUND_NOT_ALLOWED", "환불할 수 없는 상태입니다.");
        }

        return payment;
    }

    /**
     * 전액 환불 완료 처리 (트랜잭션 2)
     */
    @Transactional
    public Payment completeFullRefund(Long paymentId, String cancelReason) {
        Payment payment = paymentQueryService.getPaymentByIdInternal(paymentId);

        // 이중 검증
        if (!payment.canRefund()) {
            throw new PaymentException("REFUND_NOT_ALLOWED", "환불할 수 없는 상태입니다.");
        }

        payment.cancel(cancelReason);
        log.info("전액 환불 완료 - paymentId: {}", paymentId);
        return payment;
    }

    /**
     * 부분 환불 완료 처리 (트랜잭션 2)
     */
    @Transactional
    public Payment completePartialRefund(Long paymentId, int refundAmount, String cancelReason) {
        Payment payment = paymentQueryService.getPaymentByIdInternal(paymentId);

        // 이중 검증
        if (!payment.canRefund()) {
            throw new PaymentException("REFUND_NOT_ALLOWED", "환불할 수 없는 상태입니다.");
        }

        payment.partialCancel(refundAmount, cancelReason);
        log.info("부분 환불 완료 - paymentId: {}, refundAmount: {}", paymentId, refundAmount);
        return payment;
    }

    /**
     * 환불 불가 처리 (당일 취소)
     */
    @Transactional
    public Payment completeNoRefund(Long paymentId, String cancelReason) {
        Payment payment = paymentQueryService.getPaymentByIdInternal(paymentId);
        payment.cancelWithNoRefund(cancelReason);
        log.info("당일 취소로 환불 불가 - paymentId: {}", paymentId);
        return payment;
    }
}
