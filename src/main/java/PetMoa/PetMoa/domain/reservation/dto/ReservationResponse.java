package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.entity.ReservationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
        Long id,
        ReservationStatus status,
        LocalDateTime createdAt,
        PetSummary pet,
        HospitalReservationResponse hospitalReservation,
        List<TaxiDispatchResponse> taxiDispatch
) {
    public static ReservationResponse from(Reservation reservation) {
        List<TaxiDispatchResponse> taxiResponses = reservation.getTaxiReservations().stream()
                .map(TaxiDispatchResponse::from)
                .toList();

        return new ReservationResponse(
                reservation.getId(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                PetSummary.from(reservation.getPet()),
                HospitalReservationResponse.from(reservation.getHospitalReservation()),
                taxiResponses
        );
    }
}
