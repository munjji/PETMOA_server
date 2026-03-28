package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;

import java.time.LocalTime;

public record TimeSlotResponse(
        Long id,
        LocalTime startTime,
        LocalTime endTime,
        Integer capacity,
        Integer currentReservations,
        Boolean available
) {
    public static TimeSlotResponse from(TimeSlot timeSlot) {
        return new TimeSlotResponse(
                timeSlot.getId(),
                timeSlot.getStartTime(),
                timeSlot.getEndTime(),
                timeSlot.getCapacity(),
                timeSlot.getCurrentReservations(),
                timeSlot.getIsAvailable()
        );
    }
}
