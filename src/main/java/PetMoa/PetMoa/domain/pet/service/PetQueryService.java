package PetMoa.PetMoa.domain.pet.service;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetQueryService {

    private final PetRepository petRepository;

    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다. id=" + id));
    }

    public List<Pet> getPetsByOwnerId(Long ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }
}
