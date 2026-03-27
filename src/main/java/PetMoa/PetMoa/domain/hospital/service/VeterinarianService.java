package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.repository.VeterinarianRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VeterinarianService {

    private final VeterinarianRepository veterinarianRepository;

    public Veterinarian getVeterinarianById(Long id) {
        return veterinarianRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("수의사를 찾을 수 없습니다. id=" + id));
    }

    public List<Veterinarian> getVeterinariansByHospitalId(Long hospitalId) {
        return veterinarianRepository.findByHospitalId(hospitalId);
    }

    public List<Veterinarian> getVeterinariansByDepartment(MedicalDepartment department) {
        return veterinarianRepository.findByDepartment(department);
    }
}
