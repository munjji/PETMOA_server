package PetMoa.PetMoa.domain.taxi.dto;

public record TaxiAvailabilityResponse(
        boolean available,
        Integer estimatedFee,
        Integer estimatedArrivalMinutes,
        int availableVehicleCount
) {
    public static TaxiAvailabilityResponse notAvailable() {
        return new TaxiAvailabilityResponse(false, null, null, 0);
    }

    public static TaxiAvailabilityResponse of(int availableCount, int estimatedFee, int estimatedMinutes) {
        return new TaxiAvailabilityResponse(true, estimatedFee, estimatedMinutes, availableCount);
    }
}
