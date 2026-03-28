package PetMoa.PetMoa.domain.pet.service;

import PetMoa.PetMoa.domain.pet.dto.PetCreateRequest;
import PetMoa.PetMoa.domain.pet.dto.PetUpdateRequest;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PetCommandService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    public Pet createPet(Long ownerId, PetCreateRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. id=" + ownerId));

        Pet pet = Pet.builder()
                .name(request.name())
                .type(request.type())
                .size(request.size())
                .age(request.age())
                .weight(request.weight())
                .breed(request.breed())
                .owner(owner)
                .build();

        return petRepository.save(pet);
    }

    public void deletePet(Long id) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다. id=" + id));
        petRepository.delete(pet);
    }

    public Pet updatePet(Long id, PetUpdateRequest request) {
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("반려동물을 찾을 수 없습니다. id=" + id));
        pet.updateInfo(request.name(), request.size(), request.age(), request.weight(), request.breed());
        return pet;
    }
}
