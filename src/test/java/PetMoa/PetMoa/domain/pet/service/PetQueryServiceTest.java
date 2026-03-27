package PetMoa.PetMoa.domain.pet.service;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import PetMoa.PetMoa.domain.user.entity.User;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PetQueryServiceTest {

    @Mock
    private PetRepository petRepository;

    @InjectMocks
    private PetQueryService petQueryService;

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
    @DisplayName("반려동물 조회")
    class GetPet {

        @Test
        @DisplayName("성공: ID로 반려동물 조회")
        void getPetById_Success() {
            // given
            given(petRepository.findById(1L)).willReturn(Optional.of(testPet));

            // when
            Pet result = petQueryService.getPetById(1L);

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
            assertThatThrownBy(() -> petQueryService.getPetById(999L))
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
            List<Pet> result = petQueryService.getPetsByOwnerId(1L);

            // then
            assertThat(result).hasSize(2);
        }
    }
}
