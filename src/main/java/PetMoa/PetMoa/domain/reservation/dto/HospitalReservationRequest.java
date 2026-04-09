package PetMoa.PetMoa.domain.reservation.dto;

public record HospitalReservationRequest(
        Long timeSlotId,
        String symptomDescription
) {
}
