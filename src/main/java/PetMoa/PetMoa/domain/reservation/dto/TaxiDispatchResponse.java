package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.TaxiReservation;
import PetMoa.PetMoa.domain.reservation.entity.TaxiReservationStatus;
import PetMoa.PetMoa.domain.reservation.entity.TaxiReservationType;

import java.time.LocalDateTime;

public record TaxiDispatchResponse(
        TaxiReservationType type,
        TaxiReservationStatus status,
        String driverName,
        String driverPhoneNumber,
        String vehicleNumber,
        LocalDateTime scheduledTime,
        Integer estimatedFee
) {
    public static TaxiDispatchResponse from(TaxiReservation tr) {
        return new TaxiDispatchResponse(
                tr.getType(),
                tr.getStatus(),
                tr.getTaxi().getDriverName(),
                tr.getTaxi().getDriverPhoneNumber(),
                tr.getTaxi().getLicensePlate(),
                tr.getPickupTime(),
                tr.getFare()
        );
    }
}
