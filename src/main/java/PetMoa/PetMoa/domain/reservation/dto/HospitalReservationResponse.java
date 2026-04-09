package PetMoa.PetMoa.domain.reservation.dto;

import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;

import java.time.LocalDate;
import java.time.LocalTime;

public record HospitalReservationResponse(
        String hospitalName,
        String veterinarianName,
        LocalDate date,
        String time
) {
    public static HospitalReservationResponse from(HospitalReservation hr) {
        String timeRange = hr.getTimeSlot().getStartTime() + "-" + hr.getTimeSlot().getEndTime();
        return new HospitalReservationResponse(
                hr.getVeterinarian().getHospital().getName(),
                hr.getVeterinarian().getName(),
                hr.getTimeSlot().getDate(),
                timeRange
        );
    }
}
