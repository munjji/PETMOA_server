package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.TaxiReservationType;

import java.time.LocalDateTime;

public record TaxiRequest(
        TaxiReservationType type,
        String pickupAddress,
        String dropoffAddress,
        LocalDateTime scheduledTime
) {
}
