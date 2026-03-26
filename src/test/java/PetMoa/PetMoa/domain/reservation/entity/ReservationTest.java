package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Reservation 엔티티 테스트")
class ReservationTest {

    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .email("hong@example.com")
                .build();

        pet = Pet.builder()
                .name("먼지")
                .type(PetType.CAT)
                .size(PetSize.MEDIUM)
                .owner(user)
                .build();
    }

    @Test
    @DisplayName("Reservation 객체 생성 성공")
    void createReservation() {
        // given
        String memo = "조용한 곳으로 부탁드립니다";

        // when
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .memo(memo)
                .build();

        // then
        assertThat(reservation).isNotNull();
        assertThat(reservation.getUser()).isEqualTo(user);
        assertThat(reservation.getPet()).isEqualTo(pet);
        assertThat(reservation.getMemo()).isEqualTo(memo);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(reservation.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(사용자) 누락 시 예외 발생")
    void createReservation_withoutUser() {
        // given & when & then
        assertThatThrownBy(() -> Reservation.builder()
                .pet(pet)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("사용자");
    }

    @Test
    @DisplayName("필수 필드(반려동물) 누락 시 예외 발생")
    void createReservation_withoutPet() {
        // given & when & then
        assertThatThrownBy(() -> Reservation.builder()
                .user(user)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("반려동물");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 메모")
    void createReservation_withoutMemo() {
        // given & when
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        // then
        assertThat(reservation.getMemo()).isNull();
    }

    @Test
    @DisplayName("상태 기본값 설정 - PENDING")
    void defaultStatus() {
        // given & when
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.PENDING);
    }

    @Test
    @DisplayName("ReservationStatus Enum 값 검증")
    void validateReservationStatus() {
        // given & when & then
        assertThat(ReservationStatus.PENDING).isNotNull();
        assertThat(ReservationStatus.CONFIRMED).isNotNull();
        assertThat(ReservationStatus.CANCELLED).isNotNull();
        assertThat(ReservationStatus.COMPLETED).isNotNull();
        assertThat(ReservationStatus.NO_SHOW).isNotNull();
    }

    @Test
    @DisplayName("택시 예약 목록 초기화")
    void initializeTaxiReservations() {
        // given & when
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        // then
        assertThat(reservation.getTaxiReservations()).isNotNull();
        assertThat(reservation.getTaxiReservations()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(reservation.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("상태 변경 - PENDING to CONFIRMED")
    void changeStatus_toConfirmed() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        // when
        reservation.confirm();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    @DisplayName("상태 변경 - CONFIRMED to CANCELLED")
    void changeStatus_toCancelled() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when
        reservation.cancel();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    @DisplayName("상태 변경 - CONFIRMED to COMPLETED")
    void changeStatus_toCompleted() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when
        reservation.complete();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test
    @DisplayName("상태 변경 - CONFIRMED to NO_SHOW")
    void changeStatus_toNoShow() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when
        reservation.noShow();

        // then
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
    }

    @Test
    @DisplayName("예약 확정 가능 여부 - PENDING 상태")
    void canConfirm_pending() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        // when & then
        assertThat(reservation.canConfirm()).isTrue();
    }

    @Test
    @DisplayName("예약 확정 가능 여부 - CONFIRMED 상태")
    void canConfirm_confirmed() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when & then
        assertThat(reservation.canConfirm()).isFalse();
    }

    @Test
    @DisplayName("예약 취소 가능 여부 - CONFIRMED 상태")
    void canCancel_confirmed() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when & then
        assertThat(reservation.canCancel()).isTrue();
    }

    @Test
    @DisplayName("예약 취소 가능 여부 - COMPLETED 상태")
    void canCancel_completed() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();
        reservation.complete();

        // when & then
        assertThat(reservation.canCancel()).isFalse();
    }

    @Test
    @DisplayName("예약 확정 - PENDING 아닌 상태에서 예외 발생")
    void confirm_notPending() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();

        // when & then
        assertThatThrownBy(() -> reservation.confirm())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("대기 중인 예약만 확정");
    }

    @Test
    @DisplayName("예약 취소 - 취소 불가능한 상태에서 예외 발생")
    void cancel_cannotCancel() {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        reservation.confirm();
        reservation.complete();

        // when & then
        assertThatThrownBy(() -> reservation.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("취소할 수 없는 상태");
    }

    @Test
    @Disabled("@PreUpdate는 JPA 생명주기 콜백이므로 통합 테스트에서 검증 필요")
    @DisplayName("수정 시간 자동 업데이트")
    void autoUpdateUpdatedAt() throws InterruptedException {
        // given
        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();
        LocalDateTime createdAt = reservation.getCreatedAt();

        Thread.sleep(10); // 시간 차이를 위해 대기

        // when
        reservation.confirm();

        // then
        assertThat(reservation.getUpdatedAt()).isNotNull();
        assertThat(reservation.getUpdatedAt()).isAfter(createdAt);
    }
}
