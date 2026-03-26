package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.hospital.entity.*;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("HospitalReservation 엔티티 테스트")
class HospitalReservationTest {

    private Reservation reservation;
    private Veterinarian veterinarian;
    private TimeSlot timeSlot;

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

        timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();
    }

    @Test
    @DisplayName("HospitalReservation 객체 생성 성공")
    void createHospitalReservation() {
        // given
        VisitType visitType = VisitType.FIRST_VISIT;
        String symptoms = "기침과 재채기가 계속됩니다";

        // when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(visitType)
                .symptoms(symptoms)
                .build();

        // then
        assertThat(hospitalReservation).isNotNull();
        assertThat(hospitalReservation.getReservation()).isEqualTo(reservation);
        assertThat(hospitalReservation.getVeterinarian()).isEqualTo(veterinarian);
        assertThat(hospitalReservation.getTimeSlot()).isEqualTo(timeSlot);
        assertThat(hospitalReservation.getVisitType()).isEqualTo(visitType);
        assertThat(hospitalReservation.getSymptoms()).isEqualTo(symptoms);
        assertThat(hospitalReservation.getDepositAmount()).isEqualTo(10000);
        assertThat(hospitalReservation.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(예약) 누락 시 예외 발생")
    void createHospitalReservation_withoutReservation() {
        // given & when & then
        assertThatThrownBy(() -> HospitalReservation.builder()
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약");
    }

    @Test
    @DisplayName("필수 필드(수의사) 누락 시 예외 발생")
    void createHospitalReservation_withoutVeterinarian() {
        // given & when & then
        assertThatThrownBy(() -> HospitalReservation.builder()
                .reservation(reservation)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수의사");
    }

    @Test
    @DisplayName("필수 필드(타임슬롯) 누락 시 예외 발생")
    void createHospitalReservation_withoutTimeSlot() {
        // given & when & then
        assertThatThrownBy(() -> HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .visitType(VisitType.FIRST_VISIT)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("타임슬롯");
    }

    @Test
    @DisplayName("필수 필드(진료 유형) 누락 시 예외 발생")
    void createHospitalReservation_withoutVisitType() {
        // given & when & then
        assertThatThrownBy(() -> HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("진료 유형");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 증상")
    void createHospitalReservation_withoutSymptoms() {
        // given & when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build();

        // then
        assertThat(hospitalReservation.getSymptoms()).isNull();
    }

    @Test
    @DisplayName("예약금 기본값 설정 - 10,000원")
    void defaultDepositAmount() {
        // given & when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build();

        // then
        assertThat(hospitalReservation.getDepositAmount()).isEqualTo(10000);
    }

    @Test
    @DisplayName("예약금 커스텀 설정")
    void customDepositAmount() {
        // given
        Integer customDeposit = 20000;

        // when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .depositAmount(customDeposit)
                .build();

        // then
        assertThat(hospitalReservation.getDepositAmount()).isEqualTo(customDeposit);
    }

    @Test
    @DisplayName("예약금 검증 - 0 이하")
    void validateDepositAmount_zero() {
        // given & when & then
        assertThatThrownBy(() -> HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .depositAmount(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("예약금은 0보다 커야");
    }

    @Test
    @DisplayName("VisitType Enum 값 검증")
    void validateVisitType() {
        // given & when & then
        assertThat(VisitType.FIRST_VISIT).isNotNull();
        assertThat(VisitType.FOLLOW_UP).isNotNull();
    }

    @Test
    @DisplayName("진료 기록 목록 초기화")
    void initializeMedicalRecords() {
        // given & when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build();

        // then
        assertThat(hospitalReservation.getMedicalRecords()).isNotNull();
        assertThat(hospitalReservation.getMedicalRecords()).isEmpty();
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(hospitalReservation.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("초진 여부 확인")
    void isFirstVisit() {
        // given
        HospitalReservation firstVisit = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .build();

        HospitalReservation followUp = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FOLLOW_UP)
                .build();

        // when & then
        assertThat(firstVisit.isFirstVisit()).isTrue();
        assertThat(followUp.isFirstVisit()).isFalse();
    }
}
