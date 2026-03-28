package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;

import java.time.LocalTime;

public record VeterinarianResponse(
        Long id,
        String name,
        MedicalDepartment department,
        LocalTime workStartTime,
        LocalTime workEndTime
) {
    public static VeterinarianResponse from(Veterinarian vet) {
        return new VeterinarianResponse(
                vet.getId(),
                vet.getName(),
                vet.getDepartment(),
                vet.getWorkStartTime(),
                vet.getWorkEndTime()
        );
    }
}
