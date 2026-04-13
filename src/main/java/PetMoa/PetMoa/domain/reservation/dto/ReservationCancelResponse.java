package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.entity.ReservationStatus;

public record ReservationCancelResponse(
        Long reservationId,
        ReservationStatus status,
        RefundInfo refund
) {
    public record RefundInfo(
            Integer refundAmount,
            Integer refundRate,
            String reason,
            boolean refundProcessed
    ) {}

    public static ReservationCancelResponse from(Reservation reservation, int refundRate) {
        return from(reservation, refundRate, null);
    }

    public static ReservationCancelResponse from(Reservation reservation, int refundRate, Integer actualRefundAmount) {
        String reason = switch (refundRate) {
            case 100 -> "24시간 전 취소";
            case 50 -> "12시간 전 취소";
            default -> "당일 취소 (환불 불가)";
        };

        // 실제 환불 금액이 있으면 사용, 없으면 예상 금액 계산
        int refundAmount;
        boolean refundProcessed;

        if (actualRefundAmount != null) {
            refundAmount = actualRefundAmount;
            refundProcessed = true;
        } else {
            int totalAmount = reservation.calculateTotalAmount();
            refundAmount = totalAmount * refundRate / 100;
            refundProcessed = false;
        }

        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getStatus(),
                new RefundInfo(refundAmount, refundRate, reason, refundProcessed)
        );
    }
}
