package PetMoa.PetMoa.domain.reservation.dto;

import java.util.List;

public record ReservationCreateRequest(
        Long petId,
        HospitalReservationRequest hospitalReservation,
        List<TaxiRequest> taxiRequests
) {
}
