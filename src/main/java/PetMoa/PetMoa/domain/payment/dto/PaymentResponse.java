package PetMoa.PetMoa.domain.payment.dto;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.entity.PaymentMethod;
import PetMoa.PetMoa.domain.payment.entity.PaymentStatus;

import java.time.LocalDateTime;

public record PaymentResponse(
        Long paymentId,
        Long reservationId,
        String orderId,
        String paymentKey,
        Integer totalAmount,
        Integer depositAmount,
        Integer taxiFare,
        PaymentStatus status,
        PaymentMethod method,
        LocalDateTime paidAt,
        LocalDateTime cancelledAt,
        Integer refundAmount,
        String cancelReason
) {
    public static PaymentResponse from(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReservation().getId(),
                payment.getOrderId(),
                payment.getPaymentKey(),
                payment.getTotalAmount(),
                payment.getDepositAmount(),
                payment.getTaxiFare(),
                payment.getStatus(),
                payment.getMethod(),
                payment.getPaidAt(),
                payment.getCancelledAt(),
                payment.getRefundAmount(),
                payment.getCancelReason()
        );
    }
}
