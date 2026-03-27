package PetMoa.PetMoa.domain.taxi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TaxiAvailabilityResponse {
    private final boolean available;
    private final Integer estimatedFee;
    private final Integer estimatedArrivalMinutes;
    private final int availableVehicleCount;

    public static TaxiAvailabilityResponse notAvailable() {
        return TaxiAvailabilityResponse.builder()
                .available(false)
                .estimatedFee(null)
                .estimatedArrivalMinutes(null)
                .availableVehicleCount(0)
                .build();
    }

    public static TaxiAvailabilityResponse of(int availableCount, int estimatedFee, int estimatedMinutes) {
        return TaxiAvailabilityResponse.builder()
                .available(true)
                .estimatedFee(estimatedFee)
                .estimatedArrivalMinutes(estimatedMinutes)
                .availableVehicleCount(availableCount)
                .build();
    }
}
