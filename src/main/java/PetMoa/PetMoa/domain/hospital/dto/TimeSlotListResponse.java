package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;

import java.time.LocalDate;
import java.util.List;

public record TimeSlotListResponse(
        LocalDate date,
        VetSummary veterinarian,
        List<TimeSlotResponse> timeSlots
) {
    public record VetSummary(
            Long id,
            String name,
            MedicalDepartment department
    ) {
        public static VetSummary from(Veterinarian vet) {
            return new VetSummary(vet.getId(), vet.getName(), vet.getDepartment());
        }
    }

    public static TimeSlotListResponse from(LocalDate date, Veterinarian veterinarian, List<TimeSlot> timeSlots) {
        List<TimeSlotResponse> slotResponses = timeSlots.stream()
                .map(TimeSlotResponse::from)
                .toList();

        return new TimeSlotListResponse(
                date,
                VetSummary.from(veterinarian),
                slotResponses
        );
    }
}
