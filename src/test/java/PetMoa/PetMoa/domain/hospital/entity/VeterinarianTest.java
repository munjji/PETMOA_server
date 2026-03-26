package PetMoa.PetMoa.domain.hospital.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Veterinarian 엔티티 테스트")
class VeterinarianTest {

    private Hospital hospital;

    @BeforeEach
    void setUp() {
        hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();
    }

    @Test
    @DisplayName("Veterinarian 객체 생성 성공")
    void createVeterinarian() {
        // given
        String name = "김수의";
        MedicalDepartment department = MedicalDepartment.INTERNAL_MEDICINE;
        LocalTime workStartTime = LocalTime.of(9, 0);
        LocalTime workEndTime = LocalTime.of(18, 0);

        // when
        Veterinarian veterinarian = Veterinarian.builder()
                .name(name)
                .department(department)
                .hospital(hospital)
                .workStartTime(workStartTime)
                .workEndTime(workEndTime)
                .build();

        // then
        assertThat(veterinarian).isNotNull();
        assertThat(veterinarian.getName()).isEqualTo(name);
        assertThat(veterinarian.getDepartment()).isEqualTo(department);
        assertThat(veterinarian.getHospital()).isEqualTo(hospital);
        assertThat(veterinarian.getWorkStartTime()).isEqualTo(workStartTime);
        assertThat(veterinarian.getWorkEndTime()).isEqualTo(workEndTime);
        assertThat(veterinarian.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(이름) 누락 시 예외 발생")
    void createVeterinarian_withoutName() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("이름");
    }

    @Test
    @DisplayName("필수 필드(진료 과목) 누락 시 예외 발생")
    void createVeterinarian_withoutDepartment() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("진료 과목");
    }

    @Test
    @DisplayName("필수 필드(병원) 누락 시 예외 발생")
    void createVeterinarian_withoutHospital() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("병원");
    }

    @Test
    @DisplayName("필수 필드(근무 시작 시간) 누락 시 예외 발생")
    void createVeterinarian_withoutWorkStartTime() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workEndTime(LocalTime.of(18, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("근무 시작 시간");
    }

    @Test
    @DisplayName("필수 필드(근무 종료 시간) 누락 시 예외 발생")
    void createVeterinarian_withoutWorkEndTime() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("근무 종료 시간");
    }

    @Test
    @DisplayName("근무 시간 검증 - 종료 시간이 시작 시간보다 빠름")
    void validateWorkTime_endBeforeStart() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(18, 0))
                .workEndTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("근무 종료 시간은 시작 시간보다 늦어야 합니다");
    }

    @Test
    @DisplayName("근무 시간 검증 - 시작 시간과 종료 시간이 동일")
    void validateWorkTime_same() {
        // given & when & then
        assertThatThrownBy(() -> Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(9, 0))
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("근무 종료 시간은 시작 시간보다 늦어야 합니다");
    }

    @Test
    @DisplayName("MedicalDepartment Enum 값 검증")
    void validateMedicalDepartment() {
        // given & when & then
        assertThat(MedicalDepartment.INTERNAL_MEDICINE).isNotNull();
        assertThat(MedicalDepartment.SURGERY).isNotNull();
        assertThat(MedicalDepartment.DERMATOLOGY).isNotNull();
        assertThat(MedicalDepartment.DENTISTRY).isNotNull();
        assertThat(MedicalDepartment.OPHTHALMOLOGY).isNotNull();
        assertThat(MedicalDepartment.CARDIOLOGY).isNotNull();
        assertThat(MedicalDepartment.GENERAL).isNotNull();
    }

    @Test
    @DisplayName("TimeSlot 목록 초기화")
    void initializeTimeSlots() {
        // given & when
        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        // then
        assertThat(veterinarian.getTimeSlots()).isNotNull();
        assertThat(veterinarian.getTimeSlots()).isEmpty();
    }

    @Test
    @DisplayName("HospitalReservation 목록 초기화")
    void initializeReservations() {
        // given & when
        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        // then
        assertThat(veterinarian.getReservations()).isNotNull();
        assertThat(veterinarian.getReservations()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(veterinarian.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("근무 시간 내 여부 확인 - 근무 시간 내")
    void isWorkingTime_withinWorkTime() {
        // given
        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        // when & then
        assertThat(veterinarian.isWorkingTime(LocalTime.of(9, 0))).isTrue();
        assertThat(veterinarian.isWorkingTime(LocalTime.of(12, 0))).isTrue();
        assertThat(veterinarian.isWorkingTime(LocalTime.of(17, 59))).isTrue();
    }

    @Test
    @DisplayName("근무 시간 내 여부 확인 - 근무 시간 외")
    void isWorkingTime_outsideWorkTime() {
        // given
        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        // when & then
        assertThat(veterinarian.isWorkingTime(LocalTime.of(8, 59))).isFalse();
        assertThat(veterinarian.isWorkingTime(LocalTime.of(18, 0))).isFalse();
        assertThat(veterinarian.isWorkingTime(LocalTime.of(20, 0))).isFalse();
    }
}
