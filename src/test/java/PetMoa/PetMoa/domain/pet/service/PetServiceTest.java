package PetMoa.PetMoa.domain.pet.service;

import PetMoa.PetMoa.domain.pet.dto.PetCreateRequest;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PetService petService;

    private User testOwner;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        testOwner = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();

        testPet = Pet.builder()
                .name("뽀삐")
                .type(PetType.DOG)
                .size(PetSize.SMALL)
                .age(3)
                .weight(4.5)
                .breed("말티즈")
                .owner(testOwner)
                .build();
    }

    @Nested
    @DisplayName("반려동물 등록")
    class CreatePet {

        @Test
        @DisplayName("성공: 유효한 정보로 반려동물 등록")
        void createPet_Success() {
            // given
            PetCreateRequest request = PetCreateRequest.builder()
                    .name("뽀삐")
                    .type(PetType.DOG)
                    .size(PetSize.SMALL)
                    .age(3)
                    .weight(4.5)
                    .breed("말티즈")
                    .build();

            given(userRepository.findById(1L)).willReturn(Optional.of(testOwner));
            given(petRepository.save(any(Pet.class))).willReturn(testPet);

            // when
            Pet result = petService.createPet(1L, request);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("뽀삐");
            assertThat(result.getType()).isEqualTo(PetType.DOG);
            verify(petRepository).save(any(Pet.class));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 소유자")
        void createPet_OwnerNotFound() {
            // given
            PetCreateRequest request = PetCreateRequest.builder()
                    .name("뽀삐")
                    .type(PetType.DOG)
                    .size(PetSize.SMALL)
                    .age(3)
                    .weight(4.5)
                    .breed("말티즈")
                    .build();

            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.createPet(999L, request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("사용자를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("반려동물 조회")
    class GetPet {

        @Test
        @DisplayName("성공: ID로 반려동물 조회")
        void getPetById_Success() {
            // given
            given(petRepository.findById(1L)).willReturn(Optional.of(testPet));

            // when
            Pet result = petService.getPetById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("뽀삐");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID")
        void getPetById_NotFound() {
            // given
            given(petRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.getPetById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("반려동물을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 소유자 ID로 반려동물 목록 조회")
        void getPetsByOwnerId_Success() {
            // given
            Pet pet2 = Pet.builder()
                    .name("초코")
                    .type(PetType.DOG)
                    .size(PetSize.MEDIUM)
                    .owner(testOwner)
                    .build();
            given(petRepository.findByOwnerId(1L)).willReturn(List.of(testPet, pet2));

            // when
            List<Pet> result = petService.getPetsByOwnerId(1L);

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("반려동물 삭제")
    class DeletePet {

        @Test
        @DisplayName("성공: 반려동물 삭제")
        void deletePet_Success() {
            // given
            given(petRepository.findById(1L)).willReturn(Optional.of(testPet));

            // when
            petService.deletePet(1L);

            // then
            verify(petRepository).delete(testPet);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 반려동물 삭제")
        void deletePet_NotFound() {
            // given
            given(petRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petService.deletePet(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("반려동물을 찾을 수 없습니다");
        }
    }
}
