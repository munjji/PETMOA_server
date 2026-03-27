package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetTaxiQueryService {

    private final PetTaxiRepository petTaxiRepository;

    public PetTaxi getPetTaxiById(Long id) {
        return petTaxiRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("펫택시를 찾을 수 없습니다. id=" + id));
    }

    public List<PetTaxi> getAvailableTaxis() {
        return petTaxiRepository.findByStatus(TaxiStatus.AVAILABLE);
    }

    public List<PetTaxi> getAvailableTaxisForPetSize(PetSize petSize) {
        Set<VehicleSize> allowedVehicleSizes = getAllowedVehicleSizes(petSize);
        return petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedVehicleSizes);
    }

    private Set<VehicleSize> getAllowedVehicleSizes(PetSize petSize) {
        return switch (petSize) {
            case SMALL -> Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            case MEDIUM -> Set.of(VehicleSize.MEDIUM, VehicleSize.LARGE);
            case LARGE -> Set.of(VehicleSize.LARGE);
        };
    }
}
