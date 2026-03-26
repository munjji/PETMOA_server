package PetMoa.PetMoa.domain.hospital.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("TimeSlot 엔티티 테스트")
class TimeSlotTest {

    private Veterinarian veterinarian;

    @BeforeEach
    void setUp() {
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();
    }

    @Test
    @DisplayName("TimeSlot 객체 생성 성공")
    void createTimeSlot() {
        // given
        LocalDate date = LocalDate.of(2024, 3, 15);
        LocalTime startTime = LocalTime.of(14, 0);
        LocalTime endTime = LocalTime.of(14, 30);
        Integer capacity = 1;

        // when
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(date)
                .startTime(startTime)
                .endTime(endTime)
                .capacity(capacity)
                .build();

        // then
        assertThat(timeSlot).isNotNull();
        assertThat(timeSlot.getVeterinarian()).isEqualTo(veterinarian);
        assertThat(timeSlot.getDate()).isEqualTo(date);
        assertThat(timeSlot.getStartTime()).isEqualTo(startTime);
        assertThat(timeSlot.getEndTime()).isEqualTo(endTime);
        assertThat(timeSlot.getCapacity()).isEqualTo(capacity);
        assertThat(timeSlot.getCurrentReservations()).isEqualTo(0);
        assertThat(timeSlot.getIsAvailable()).isTrue();
        assertThat(timeSlot.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(수의사) 누락 시 예외 발생")
    void createTimeSlot_withoutVeterinarian() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수의사");
    }

    @Test
    @DisplayName("필수 필드(날짜) 누락 시 예외 발생")
    void createTimeSlot_withoutDate() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("날짜");
    }

    @Test
    @DisplayName("필수 필드(시작 시간) 누락 시 예외 발생")
    void createTimeSlot_withoutStartTime() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .endTime(LocalTime.of(14, 30))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("시작 시간");
    }

    @Test
    @DisplayName("필수 필드(종료 시간) 누락 시 예외 발생")
    void createTimeSlot_withoutEndTime() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간");
    }

    @Test
    @DisplayName("시간 검증 - 종료 시간이 시작 시간보다 빠름")
    void validateTime_endBeforeStart() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(14, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간은 시작 시간보다 늦어야 합니다");
    }

    @Test
    @DisplayName("시간 검증 - 시작 시간과 종료 시간이 동일")
    void validateTime_same() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("종료 시간은 시작 시간보다 늦어야 합니다");
    }

    @Test
    @DisplayName("정원 기본값 설정 - 1")
    void defaultCapacity() {
        // given & when
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        // then
        assertThat(timeSlot.getCapacity()).isEqualTo(1);
    }

    @Test
    @DisplayName("현재 예약 수 기본값 설정 - 0")
    void defaultCurrentReservations() {
        // given & when
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        // then
        assertThat(timeSlot.getCurrentReservations()).isEqualTo(0);
    }

    @Test
    @DisplayName("예약 가능 여부 기본값 설정 - true")
    void defaultIsAvailable() {
        // given & when
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        // then
        assertThat(timeSlot.getIsAvailable()).isTrue();
    }

    @Test
    @DisplayName("정원 검증 - 0 이하")
    void validateCapacity_zero() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정원은 1 이상");
    }

    @Test
    @DisplayName("정원 검증 - 음수")
    void validateCapacity_negative() {
        // given & when & then
        assertThatThrownBy(() -> TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(-1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("정원은 1 이상");
    }

    @Test
    @DisplayName("예약 가능 여부 확인 - 여유 있음")
    void isAvailable_hasSpace() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(2)
                .build();

        // when & then
        assertThat(timeSlot.hasAvailableSpace()).isTrue();
    }

    @Test
    @DisplayName("예약 가능 여부 확인 - 마감")
    void isAvailable_full() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(1)
                .build();

        // when
        timeSlot.incrementReservations();

        // then
        assertThat(timeSlot.hasAvailableSpace()).isFalse();
    }

    @Test
    @DisplayName("예약 수 증가")
    void incrementReservations() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(2)
                .build();

        // when
        timeSlot.incrementReservations();

        // then
        assertThat(timeSlot.getCurrentReservations()).isEqualTo(1);
        assertThat(timeSlot.hasAvailableSpace()).isTrue();
    }

    @Test
    @DisplayName("예약 수 증가 - 정원 초과 시 예외 발생")
    void incrementReservations_exceedCapacity() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(1)
                .build();

        timeSlot.incrementReservations();

        // when & then
        assertThatThrownBy(() -> timeSlot.incrementReservations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("예약이 마감");
    }

    @Test
    @DisplayName("예약 수 감소")
    void decrementReservations() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .capacity(1)
                .build();

        timeSlot.incrementReservations();

        // when
        timeSlot.decrementReservations();

        // then
        assertThat(timeSlot.getCurrentReservations()).isEqualTo(0);
        assertThat(timeSlot.hasAvailableSpace()).isTrue();
    }

    @Test
    @DisplayName("예약 수 감소 - 0보다 작아질 수 없음")
    void decrementReservations_belowZero() {
        // given
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        // when & then
        assertThatThrownBy(() -> timeSlot.decrementReservations())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("예약 수는 0보다 작을 수 없습니다");
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(timeSlot.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("30분 단위 슬롯 검증")
    void validate30MinuteSlot() {
        // given & when
        TimeSlot timeSlot1 = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        TimeSlot timeSlot2 = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 30))
                .endTime(LocalTime.of(15, 0))
                .build();

        // then
        assertThat(timeSlot1.getStartTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(timeSlot1.getEndTime()).isEqualTo(LocalTime.of(14, 30));
        assertThat(timeSlot2.getStartTime()).isEqualTo(LocalTime.of(14, 30));
        assertThat(timeSlot2.getEndTime()).isEqualTo(LocalTime.of(15, 0));
    }
}
