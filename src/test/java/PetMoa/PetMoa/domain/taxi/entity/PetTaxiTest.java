package PetMoa.PetMoa.domain.taxi.entity;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PetTaxi 엔티티 테스트")
class PetTaxiTest {

    @Test
    @DisplayName("PetTaxi 객체 생성 성공")
    void createPetTaxi() {
        // given
        String licensePlate = "12가3456";
        String driverName = "김기사";
        String driverPhoneNumber = "010-1234-5678";
        VehicleSize vehicleSize = VehicleSize.MEDIUM;
        TaxiStatus status = TaxiStatus.AVAILABLE;

        // when
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate(licensePlate)
                .driverName(driverName)
                .driverPhoneNumber(driverPhoneNumber)
                .vehicleSize(vehicleSize)
                .status(status)
                .build();

        // then
        assertThat(petTaxi).isNotNull();
        assertThat(petTaxi.getLicensePlate()).isEqualTo(licensePlate);
        assertThat(petTaxi.getDriverName()).isEqualTo(driverName);
        assertThat(petTaxi.getDriverPhoneNumber()).isEqualTo(driverPhoneNumber);
        assertThat(petTaxi.getVehicleSize()).isEqualTo(vehicleSize);
        assertThat(petTaxi.getStatus()).isEqualTo(status);
        assertThat(petTaxi.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(차량 번호) 누락 시 예외 발생")
    void createPetTaxi_withoutLicensePlate() {
        // given & when & then
        assertThatThrownBy(() -> PetTaxi.builder()
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("차량 번호");
    }

    @Test
    @DisplayName("필수 필드(기사 이름) 누락 시 예외 발생")
    void createPetTaxi_withoutDriverName() {
        // given & when & then
        assertThatThrownBy(() -> PetTaxi.builder()
                .licensePlate("12가3456")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기사 이름");
    }

    @Test
    @DisplayName("필수 필드(기사 연락처) 누락 시 예외 발생")
    void createPetTaxi_withoutDriverPhoneNumber() {
        // given & when & then
        assertThatThrownBy(() -> PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .vehicleSize(VehicleSize.MEDIUM)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("기사 연락처");
    }

    @Test
    @DisplayName("필수 필드(차량 크기) 누락 시 예외 발생")
    void createPetTaxi_withoutVehicleSize() {
        // given & when & then
        assertThatThrownBy(() -> PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("차량 크기");
    }

    @Test
    @DisplayName("상태 기본값 설정 - AVAILABLE")
    void defaultStatus() {
        // given & when
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();

        // then
        assertThat(petTaxi.getStatus()).isEqualTo(TaxiStatus.AVAILABLE);
    }

    @Test
    @DisplayName("VehicleSize Enum 값 검증")
    void validateVehicleSize() {
        // given & when & then
        assertThat(VehicleSize.SMALL).isNotNull();
        assertThat(VehicleSize.MEDIUM).isNotNull();
        assertThat(VehicleSize.LARGE).isNotNull();
    }

    @Test
    @DisplayName("TaxiStatus Enum 값 검증")
    void validateTaxiStatus() {
        // given & when & then
        assertThat(TaxiStatus.AVAILABLE).isNotNull();
        assertThat(TaxiStatus.IN_SERVICE).isNotNull();
        assertThat(TaxiStatus.MAINTENANCE).isNotNull();
        assertThat(TaxiStatus.UNAVAILABLE).isNotNull();
    }

    @Test
    @DisplayName("예약 목록 초기화")
    void initializeReservations() {
        // given & when
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();

        // then
        assertThat(petTaxi.getReservations()).isNotNull();
        assertThat(petTaxi.getReservations()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(petTaxi.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("반려동물 탑승 가능 여부 - 소형 차량")
    void canAccommodate_small() {
        // given
        PetTaxi smallTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.SMALL)
                .build();

        // when & then
        assertThat(smallTaxi.canAccommodate(PetSize.SMALL)).isTrue();
        assertThat(smallTaxi.canAccommodate(PetSize.MEDIUM)).isFalse();
        assertThat(smallTaxi.canAccommodate(PetSize.LARGE)).isFalse();
    }

    @Test
    @DisplayName("반려동물 탑승 가능 여부 - 중형 차량")
    void canAccommodate_medium() {
        // given
        PetTaxi mediumTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();

        // when & then
        assertThat(mediumTaxi.canAccommodate(PetSize.SMALL)).isTrue();
        assertThat(mediumTaxi.canAccommodate(PetSize.MEDIUM)).isTrue();
        assertThat(mediumTaxi.canAccommodate(PetSize.LARGE)).isFalse();
    }

    @Test
    @DisplayName("반려동물 탑승 가능 여부 - 대형 차량")
    void canAccommodate_large() {
        // given
        PetTaxi largeTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.LARGE)
                .build();

        // when & then
        assertThat(largeTaxi.canAccommodate(PetSize.SMALL)).isTrue();
        assertThat(largeTaxi.canAccommodate(PetSize.MEDIUM)).isTrue();
        assertThat(largeTaxi.canAccommodate(PetSize.LARGE)).isTrue();
    }

    @Test
    @DisplayName("예약 가능 상태 확인 - AVAILABLE")
    void isAvailable_available() {
        // given
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .status(TaxiStatus.AVAILABLE)
                .build();

        // when & then
        assertThat(petTaxi.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("예약 가능 상태 확인 - IN_SERVICE")
    void isAvailable_inService() {
        // given
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .status(TaxiStatus.IN_SERVICE)
                .build();

        // when & then
        assertThat(petTaxi.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("상태 변경")
    void changeStatus() {
        // given
        PetTaxi petTaxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();

        // when
        petTaxi.changeStatus(TaxiStatus.IN_SERVICE);

        // then
        assertThat(petTaxi.getStatus()).isEqualTo(TaxiStatus.IN_SERVICE);
    }

    @Test
    @DisplayName("전화번호 형식 검증 - 유효하지 않은 형식")
    void validatePhoneNumber_invalid() {
        // given & when & then
        assertThatThrownBy(() -> PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("123")
                .vehicleSize(VehicleSize.MEDIUM)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전화번호 형식");
    }
}
