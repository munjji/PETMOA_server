package PetMoa.PetMoa.domain.hospital.entity;

import PetMoa.PetMoa.domain.pet.entity.PetType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Hospital 엔티티 테스트")
class HospitalTest {

    @Test
    @DisplayName("Hospital 객체 생성 성공")
    void createHospital() {
        // given
        String name = "강남동물병원";
        String address = "서울시 강남구 테헤란로 123";
        String phoneNumber = "02-1234-5678";
        Double latitude = 37.5665;
        Double longitude = 126.9780;
        Set<PetType> availablePetTypes = Set.of(PetType.DOG, PetType.CAT);

        // when
        Hospital hospital = Hospital.builder()
                .name(name)
                .address(address)
                .phoneNumber(phoneNumber)
                .latitude(latitude)
                .longitude(longitude)
                .availablePetTypes(availablePetTypes)
                .build();

        // then
        assertThat(hospital).isNotNull();
        assertThat(hospital.getName()).isEqualTo(name);
        assertThat(hospital.getAddress()).isEqualTo(address);
        assertThat(hospital.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(hospital.getLatitude()).isEqualTo(latitude);
        assertThat(hospital.getLongitude()).isEqualTo(longitude);
        assertThat(hospital.getAvailablePetTypes()).containsExactlyInAnyOrder(PetType.DOG, PetType.CAT);
        assertThat(hospital.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(병원 이름) 누락 시 예외 발생")
    void createHospital_withoutName() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("병원 이름");
    }

    @Test
    @DisplayName("필수 필드(주소) 누락 시 예외 발생")
    void createHospital_withoutAddress() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .name("강남동물병원")
                .phoneNumber("02-1234-5678")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("주소");
    }

    @Test
    @DisplayName("필수 필드(연락처) 누락 시 예외 발생")
    void createHospital_withoutPhoneNumber() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("연락처");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 위도, 경도, 진료 가능 동물 종류")
    void createHospital_withoutOptionalFields() {
        // given & when
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        // then
        assertThat(hospital.getLatitude()).isNull();
        assertThat(hospital.getLongitude()).isNull();
        assertThat(hospital.getAvailablePetTypes()).isNotNull();
        assertThat(hospital.getAvailablePetTypes()).isEmpty();
    }

    @Test
    @DisplayName("진료 가능 동물 종류 추가")
    void addAvailablePetType() {
        // given
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        // when
        hospital.addAvailablePetType(PetType.DOG);
        hospital.addAvailablePetType(PetType.CAT);

        // then
        assertThat(hospital.getAvailablePetTypes()).containsExactlyInAnyOrder(PetType.DOG, PetType.CAT);
    }

    @Test
    @DisplayName("진료 가능 동물 종류 중복 추가 방지")
    void addAvailablePetType_duplicate() {
        // given
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        // when
        hospital.addAvailablePetType(PetType.DOG);
        hospital.addAvailablePetType(PetType.DOG);

        // then
        assertThat(hospital.getAvailablePetTypes()).hasSize(1);
        assertThat(hospital.getAvailablePetTypes()).containsExactly(PetType.DOG);
    }

    @Test
    @DisplayName("위도 범위 검증 - 유효하지 않은 범위")
    void validateLatitude_invalid() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .latitude(100.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("위도");
    }

    @Test
    @DisplayName("경도 범위 검증 - 유효하지 않은 범위")
    void validateLongitude_invalid() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .longitude(200.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("경도");
    }

    @Test
    @DisplayName("전화번호 형식 검증 - 유효하지 않은 형식")
    void validatePhoneNumber_invalid() {
        // given & when & then
        assertThatThrownBy(() -> Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("123")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전화번호 형식");
    }

    @Test
    @DisplayName("전화번호 형식 검증 - 유효한 형식")
    void validatePhoneNumber_valid() {
        // given & when
        Hospital hospital1 = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        Hospital hospital2 = Hospital.builder()
                .name("서초동물병원")
                .address("서울시 서초구 서초대로 456")
                .phoneNumber("0212345678")
                .build();

        Hospital hospital3 = Hospital.builder()
                .name("송파동물병원")
                .address("서울시 송파구 올림픽로 789")
                .phoneNumber("010-9876-5432")
                .build();

        // then
        assertThat(hospital1.getPhoneNumber()).isEqualTo("02-1234-5678");
        assertThat(hospital2.getPhoneNumber()).isEqualTo("0212345678");
        assertThat(hospital3.getPhoneNumber()).isEqualTo("010-9876-5432");
    }

    @Test
    @DisplayName("수의사 목록 초기화")
    void initializeVeterinarians() {
        // given & when
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        // then
        assertThat(hospital.getVeterinarians()).isNotNull();
        assertThat(hospital.getVeterinarians()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(hospital.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("진료 가능 동물 확인")
    void canTreat() {
        // given
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .availablePetTypes(Set.of(PetType.DOG, PetType.CAT))
                .build();

        // when & then
        assertThat(hospital.canTreat(PetType.DOG)).isTrue();
        assertThat(hospital.canTreat(PetType.CAT)).isTrue();
        assertThat(hospital.canTreat(PetType.RABBIT)).isFalse();
    }

    @Test
    @DisplayName("모든 동물 진료 가능 - availablePetTypes가 비어있을 때")
    void canTreat_allTypes() {
        // given
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        // when & then
        assertThat(hospital.canTreat(PetType.DOG)).isTrue();
        assertThat(hospital.canTreat(PetType.CAT)).isTrue();
        assertThat(hospital.canTreat(PetType.RABBIT)).isTrue();
    }
}
