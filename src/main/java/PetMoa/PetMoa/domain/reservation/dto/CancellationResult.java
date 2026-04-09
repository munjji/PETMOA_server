package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;

public record CancellationResult(
        Reservation reservation,
        int refundRate
) {
    public static CancellationResult of(Reservation reservation, int refundRate) {
        return new CancellationResult(reservation, refundRate);
    }
}
