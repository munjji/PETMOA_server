package PetMoa.PetMoa.domain.pet.entity;

import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Pet 엔티티 테스트")
class PetTest {

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();
    }

    @Test
    @DisplayName("Pet 객체 생성 성공")
    void createPet() {
        // given
        String name = "먼지";
        PetType type = PetType.CAT;
        PetSize size = PetSize.MEDIUM;
        Integer age = 3;
        Double weight = 3.7;
        String breed = "러시안블루";

        // when
        Pet pet = Pet.builder()
                .name(name)
                .type(type)
                .size(size)
                .age(age)
                .weight(weight)
                .breed(breed)
                .owner(owner)
                .build();

        // then
        assertThat(pet).isNotNull();
        assertThat(pet.getName()).isEqualTo(name);
        assertThat(pet.getType()).isEqualTo(type);
        assertThat(pet.getSize()).isEqualTo(size);
        assertThat(pet.getAge()).isEqualTo(age);
        assertThat(pet.getWeight()).isEqualTo(weight);
        assertThat(pet.getBreed()).isEqualTo(breed);
        assertThat(pet.getOwner()).isEqualTo(owner);
        assertThat(pet.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(이름) 누락 시 예외 발생")
    void createPet_withoutName() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .owner(owner)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름");
    }

    @Test
    @DisplayName("필수 필드(종류) 누락 시 예외 발생")
    void createPet_withoutType() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .name("몽이")
                .size(PetSize.MEDIUM)
                .owner(owner)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종류");
    }

    @Test
    @DisplayName("필수 필드(크기) 누락 시 예외 발생")
    void createPet_withoutSize() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .owner(owner)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("크기");
    }

    @Test
    @DisplayName("필수 필드(소유자) 누락 시 예외 발생")
    void createPet_withoutOwner() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("소유자");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 나이, 몸무게, 품종")
    void createPet_withoutOptionalFields() {
        // given & when
        Pet pet = Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .owner(owner)
                .build();

        // then
        assertThat(pet.getAge()).isNull();
        assertThat(pet.getWeight()).isNull();
        assertThat(pet.getBreed()).isNull();
    }

    @Test
    @DisplayName("PetType Enum 값 검증")
    void validatePetType() {
        // given & when & then
        assertThat(PetType.DOG).isNotNull();
        assertThat(PetType.CAT).isNotNull();
        assertThat(PetType.RABBIT).isNotNull();
        assertThat(PetType.HAMSTER).isNotNull();
        assertThat(PetType.BIRD).isNotNull();
        assertThat(PetType.REPTILE).isNotNull();
        assertThat(PetType.ETC).isNotNull();
    }

    @Test
    @DisplayName("PetSize Enum 값 검증")
    void validatePetSize() {
        // given & when & then
        assertThat(PetSize.SMALL).isNotNull();
        assertThat(PetSize.MEDIUM).isNotNull();
        assertThat(PetSize.LARGE).isNotNull();
    }

    @Test
    @DisplayName("PetSize에 따른 무게 기준 확인")
    void validatePetSizeWeight() {
        // given & when
        Pet smallPet = Pet.builder()
                .name("작은이")
                .type(PetType.CAT)
                .size(PetSize.SMALL)
                .weight(4.0)
                .owner(owner)
                .build();

        Pet mediumPet = Pet.builder()
                .name("중간이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .weight(10.0)
                .owner(owner)
                .build();

        Pet largePet = Pet.builder()
                .name("큰이")
                .type(PetType.DOG)
                .size(PetSize.LARGE)
                .weight(20.0)
                .owner(owner)
                .build();

        // then
        assertThat(smallPet.getSize()).isEqualTo(PetSize.SMALL);
        assertThat(mediumPet.getSize()).isEqualTo(PetSize.MEDIUM);
        assertThat(largePet.getSize()).isEqualTo(PetSize.LARGE);
    }

    @Test
    @DisplayName("나이는 0 이상이어야 함")
    void validateAge_negative() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .age(-1)
                .owner(owner)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("나이");
    }

    @Test
    @DisplayName("몸무게는 0보다 커야 함")
    void validateWeight_zero() {
        // given & when & then
        assertThatThrownBy(() -> Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .weight(0.0)
                .owner(owner)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("몸무게");
    }

    @Test
    @DisplayName("진료 기록 목록 초기화")
    void initializeMedicalRecords() {
        // given & when
        Pet pet = Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .owner(owner)
                .build();

        // then
        assertThat(pet.getMedicalRecords()).isNotNull();
        assertThat(pet.getMedicalRecords()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Pet pet = Pet.builder()
                .name("몽이")
                .type(PetType.DOG)
                .size(PetSize.MEDIUM)
                .owner(owner)
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(pet.getCreatedAt()).isBetween(before, after);
    }
}
