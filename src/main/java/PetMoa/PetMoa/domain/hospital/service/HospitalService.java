package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.repository.HospitalRepository;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public Hospital getHospitalById(Long id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("병원을 찾을 수 없습니다. id=" + id));
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<Hospital> searchByName(String name) {
        return hospitalRepository.findByNameContaining(name);
    }

    public List<Hospital> searchByAddress(String address) {
        return hospitalRepository.findByAddressContaining(address);
    }

    public List<Hospital> searchByPetType(PetType petType) {
        return hospitalRepository.findByAvailablePetType(petType);
    }

    public List<Hospital> searchNearby(Double latitude, Double longitude, Double radiusKm) {
        return hospitalRepository.findNearbyHospitals(latitude, longitude, radiusKm);
    }
}
