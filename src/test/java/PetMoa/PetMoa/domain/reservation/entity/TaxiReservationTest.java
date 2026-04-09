package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TaxiReservation 엔티티 테스트")
class TaxiReservationTest {

    private Reservation reservation;
    private PetTaxi taxi;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        Pet pet = Pet.builder()
                .name("먼지")
                .type(PetType.CAT)
                .size(PetSize.MEDIUM)
                .owner(user)
                .build();

        reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        taxi = PetTaxi.builder()
                .licensePlate("12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-9876-5432")
                .vehicleSize(VehicleSize.MEDIUM)
                .build();
    }

    @Test
    @DisplayName("TaxiReservation 객체 생성 성공")
    void createTaxiReservation() {
        // given
        String pickupAddress = "서울시 강남구 테헤란로 100";
        String dropoffAddress = "서울시 강남구 테헤란로 123";
        LocalDateTime pickupTime = LocalDateTime.of(2024, 3, 15, 13, 30);
        Double distance = 5.5;
        Integer fare = 10500;
        TaxiReservationType type = TaxiReservationType.PICKUP;

        // when
        TaxiReservation taxiReservation = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress(pickupAddress)
                .dropoffAddress(dropoffAddress)
                .pickupTime(pickupTime)
                .distance(distance)
                .fare(fare)
                .type(type)
                .build();

        // then
        assertThat(taxiReservation).isNotNull();
        assertThat(taxiReservation.getReservation()).isEqualTo(reservation);
        assertThat(taxiReservation.getTaxi()).isEqualTo(taxi);
        assertThat(taxiReservation.getPickupAddress()).isEqualTo(pickupAddress);
        assertThat(taxiReservation.getDropoffAddress()).isEqualTo(dropoffAddress);
        assertThat(taxiReservation.getPickupTime()).isEqualTo(pickupTime);
        assertThat(taxiReservation.getDistance()).isEqualTo(distance);
        assertThat(taxiReservation.getFare()).isEqualTo(fare);
        assertThat(taxiReservation.getType()).isEqualTo(type);
        assertThat(taxiReservation.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(예약) 누락 시 예외 발생")
    void createTaxiReservation_withoutReservation() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약");
    }

    @Test
    @DisplayName("필수 필드(택시) 누락 시 예외 발생")
    void createTaxiReservation_withoutTaxi() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("택시");
    }

    @Test
    @DisplayName("필수 필드(픽업 주소) 누락 시 예외 발생")
    void createTaxiReservation_withoutPickupAddress() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("픽업 주소");
    }

    @Test
    @DisplayName("필수 필드(목적지 주소) 누락 시 예외 발생")
    void createTaxiReservation_withoutDropoffAddress() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("목적지 주소");
    }

    @Test
    @DisplayName("필수 필드(픽업 시간) 누락 시 예외 발생")
    void createTaxiReservation_withoutPickupTime() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("픽업 시간");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 거리, 요금, 유형")
    void createTaxiReservation_withoutOptionalFields() {
        // given & when
        TaxiReservation taxiReservation = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build();

        // then
        assertThat(taxiReservation.getDistance()).isNull();
        assertThat(taxiReservation.getFare()).isNull();
        assertThat(taxiReservation.getType()).isNull();
    }

    @Test
    @DisplayName("거리 검증 - 0 이하")
    void validateDistance_zero() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .distance(0.0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("거리는 0보다 커야");
    }

    @Test
    @DisplayName("요금 검증 - 0 이하")
    void validateFare_zero() {
        // given & when & then
        assertThatThrownBy(() -> TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .fare(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("요금은 0보다 커야");
    }

    @Test
    @DisplayName("TaxiReservationType Enum 값 검증")
    void validateTaxiReservationType() {
        // given & when & then
        assertThat(TaxiReservationType.PICKUP).isNotNull();
        assertThat(TaxiReservationType.RETURN).isNotNull();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        TaxiReservation taxiReservation = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(taxiReservation.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("요금 자동 계산 - 기본 5,000원 + km당 1,000원")
    void calculateFare() {
        // given
        Double distance = 5.5;

        // when
        TaxiReservation taxiReservation = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .distance(distance)
                .build();

        // then
        // 5,000 + (5.5 * 1,000) = 10,500
        assertThat(taxiReservation.getFare()).isEqualTo(10500);
    }

    @Test
    @DisplayName("픽업/귀가 여부 확인")
    void isPickupOrReturn() {
        // given
        TaxiReservation pickup = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 100")
                .dropoffAddress("서울시 강남구 테헤란로 123")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 13, 30))
                .type(TaxiReservationType.PICKUP)
                .build();

        TaxiReservation returnTrip = TaxiReservation.builder()
                .reservation(reservation)
                .taxi(taxi)
                .pickupAddress("서울시 강남구 테헤란로 123")
                .dropoffAddress("서울시 강남구 테헤란로 100")
                .pickupTime(LocalDateTime.of(2024, 3, 15, 15, 30))
                .type(TaxiReservationType.RETURN)
                .build();

        // when & then
        assertThat(pickup.isPickup()).isTrue();
        assertThat(pickup.isReturn()).isFalse();
        assertThat(returnTrip.isPickup()).isFalse();
        assertThat(returnTrip.isReturn()).isTrue();
    }
}
