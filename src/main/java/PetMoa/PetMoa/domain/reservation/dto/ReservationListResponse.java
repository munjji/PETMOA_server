package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;

import java.util.List;

public record ReservationListResponse(
        List<ReservationResponse> reservations
) {
    public static ReservationListResponse from(List<Reservation> reservations) {
        List<ReservationResponse> responses = reservations.stream()
                .map(ReservationResponse::from)
                .toList();
        return new ReservationListResponse(responses);
    }
}
