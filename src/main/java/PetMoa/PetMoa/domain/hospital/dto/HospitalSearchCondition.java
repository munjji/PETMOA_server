package PetMoa.PetMoa.domain.hospital.dto;

import PetMoa.PetMoa.domain.pet.entity.PetType;

public record HospitalSearchCondition(
        PetType petType,
        Double latitude,
        Double longitude,
        Double radiusKm,
        String name,
        String address
) {
    public boolean hasLocation() {
        return latitude != null && longitude != null;
    }

    public boolean hasName() {
        return name != null && !name.isBlank();
    }

    public boolean hasAddress() {
        return address != null && !address.isBlank();
    }

    public Double getRadiusOrDefault() {
        return radiusKm != null && radiusKm > 0 ? radiusKm : 5.0;
    }
}
