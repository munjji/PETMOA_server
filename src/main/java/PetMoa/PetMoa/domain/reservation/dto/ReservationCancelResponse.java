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
            String reason
    ) {}

    public static ReservationCancelResponse from(Reservation reservation, int refundRate) {
        int totalAmount = reservation.calculateTotalAmount();
        int refundAmount = totalAmount * refundRate / 100;

        String reason = switch (refundRate) {
            case 100 -> "24시간 전 취소";
            case 50 -> "12시간 전 취소";
            default -> "당일 취소 (환불 불가)";
        };

        return new ReservationCancelResponse(
                reservation.getId(),
                reservation.getStatus(),
                new RefundInfo(refundAmount, refundRate, reason)
        );
    }
}
