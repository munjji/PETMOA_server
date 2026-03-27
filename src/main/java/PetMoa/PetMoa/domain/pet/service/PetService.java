package PetMoa.PetMoa.domain.pet.service;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;

    @Transactional
    public Pet createPet(Long ownerId, String name, PetType type, PetSize size, Integer age, Double weight, String breed) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + ownerId));

        Pet pet = Pet.builder()
                .name(name)
                .type(type)
                .size(size)
                .age(age)
                .weight(weight)
                .breed(breed)
                .owner(owner)
                .build();

        return petRepository.save(pet);
    }

    public Pet getPetById(Long id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("반려동물을 찾을 수 없습니다. id=" + id));
    }

    public List<Pet> getPetsByOwnerId(Long ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }

    @Transactional
    public void deletePet(Long id) {
        Pet pet = getPetById(id);
        petRepository.delete(pet);
    }
}
