package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetTaxiService {

    private final PetTaxiRepository petTaxiRepository;

    public PetTaxi getPetTaxiById(Long id) {
        return petTaxiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("펫택시를 찾을 수 없습니다. id=" + id));
    }

    public List<PetTaxi> getAvailableTaxis() {
        return petTaxiRepository.findByStatus(TaxiStatus.AVAILABLE);
    }

    public List<PetTaxi> getAvailableTaxisForPetSize(PetSize petSize) {
        return getAvailableTaxis().stream()
                .filter(taxi -> taxi.canAccommodate(petSize))
                .toList();
    }
}
