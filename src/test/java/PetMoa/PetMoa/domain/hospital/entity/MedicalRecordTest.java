package PetMoa.PetMoa.domain.hospital.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.entity.VisitType;
import PetMoa.PetMoa.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("MedicalRecord 엔티티 테스트")
class MedicalRecordTest {

    private HospitalReservation hospitalReservation;
    private Pet pet;

    @BeforeEach
    void setUp() {
        User user = User.builder()
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

        Reservation reservation = Reservation.builder()
                .user(user)
                .pet(pet)
                .build();

        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();

        Veterinarian veterinarian = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.INTERNAL_MEDICINE)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        TimeSlot timeSlot = TimeSlot.builder()
                .veterinarian(veterinarian)
                .date(LocalDate.of(2024, 3, 15))
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(14, 30))
                .build();

        hospitalReservation = HospitalReservation.builder()
                .reservation(reservation)
                .veterinarian(veterinarian)
                .timeSlot(timeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .symptoms("기침과 재채기가 계속됩니다")
                .build();
    }

    @Test
    @DisplayName("MedicalRecord 객체 생성 성공")
    void createMedicalRecord() {
        // given
        String diagnosis = "고양이 감기";
        String treatment = "항생제 처방 및 수액 치료";
        String prescription = "항생제 5일분";
        Integer treatmentCost = 50000;
        String notes = "3일 후 재진 필요";
        LocalDateTime visitDate = LocalDateTime.of(2024, 3, 15, 14, 0);

        // when
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis(diagnosis)
                .treatment(treatment)
                .prescription(prescription)
                .treatmentCost(treatmentCost)
                .notes(notes)
                .visitDate(visitDate)
                .build();

        // then
        assertThat(medicalRecord).isNotNull();
        assertThat(medicalRecord.getHospitalReservation()).isEqualTo(hospitalReservation);
        assertThat(medicalRecord.getPet()).isEqualTo(pet);
        assertThat(medicalRecord.getDiagnosis()).isEqualTo(diagnosis);
        assertThat(medicalRecord.getTreatment()).isEqualTo(treatment);
        assertThat(medicalRecord.getPrescription()).isEqualTo(prescription);
        assertThat(medicalRecord.getTreatmentCost()).isEqualTo(treatmentCost);
        assertThat(medicalRecord.getNotes()).isEqualTo(notes);
        assertThat(medicalRecord.getVisitDate()).isEqualTo(visitDate);
        assertThat(medicalRecord.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("필수 필드(병원 예약) 누락 시 예외 발생")
    void createMedicalRecord_withoutHospitalReservation() {
        // given & when & then
        assertThatThrownBy(() -> MedicalRecord.builder()
                .pet(pet)
                .diagnosis("고양이 감기")
                .visitDate(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("병원 예약");
    }

    @Test
    @DisplayName("필수 필드(반려동물) 누락 시 예외 발생")
    void createMedicalRecord_withoutPet() {
        // given & when & then
        assertThatThrownBy(() -> MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .diagnosis("고양이 감기")
                .visitDate(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("반려동물");
    }

    @Test
    @DisplayName("필수 필드(진단명) 누락 시 예외 발생")
    void createMedicalRecord_withoutDiagnosis() {
        // given & when & then
        assertThatThrownBy(() -> MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .visitDate(LocalDateTime.now())
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("진단명");
    }

    @Test
    @DisplayName("필수 필드(진료 일시) 누락 시 예외 발생")
    void createMedicalRecord_withoutVisitDate() {
        // given & when & then
        assertThatThrownBy(() -> MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("고양이 감기")
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("진료 일시");
    }

    @Test
    @DisplayName("선택 필드 누락 허용 - 처치, 처방, 진료비, 특이사항")
    void createMedicalRecord_withoutOptionalFields() {
        // given & when
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("경미한 외상")
                .visitDate(LocalDateTime.now())
                .build();

        // then
        assertThat(medicalRecord.getTreatment()).isNull();
        assertThat(medicalRecord.getPrescription()).isNull();
        assertThat(medicalRecord.getTreatmentCost()).isNull();
        assertThat(medicalRecord.getNotes()).isNull();
    }

    @Test
    @DisplayName("진료비 검증 - 0 이하")
    void validateTreatmentCost_zero() {
        // given & when & then
        assertThatThrownBy(() -> MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("고양이 감기")
                .visitDate(LocalDateTime.now())
                .treatmentCost(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("진료비는 0보다 커야");
    }

    @Test
    @DisplayName("생성 시간 자동 설정")
    void autoSetCreatedAt() {
        // given
        LocalDateTime before = LocalDateTime.now();

        // when
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("고양이 감기")
                .visitDate(LocalDateTime.now())
                .build();

        LocalDateTime after = LocalDateTime.now();

        // then
        assertThat(medicalRecord.getCreatedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("진료 기록 요약 생성")
    void getSummary() {
        // given
        MedicalRecord medicalRecord = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("고양이 감기")
                .treatment("항생제 처방")
                .visitDate(LocalDateTime.of(2024, 3, 15, 14, 0))
                .build();

        // when
        String summary = medicalRecord.getSummary();

        // then
        assertThat(summary).contains("고양이 감기");
        assertThat(summary).contains("항생제 처방");
    }

    @Test
    @DisplayName("처방전 있는지 확인")
    void hasPrescription() {
        // given
        MedicalRecord withPrescription = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("고양이 감기")
                .prescription("항생제 5일분")
                .visitDate(LocalDateTime.now())
                .build();

        MedicalRecord withoutPrescription = MedicalRecord.builder()
                .hospitalReservation(hospitalReservation)
                .pet(pet)
                .diagnosis("경미한 외상")
                .visitDate(LocalDateTime.now())
                .build();

        // when & then
        assertThat(withPrescription.hasPrescription()).isTrue();
        assertThat(withoutPrescription.hasPrescription()).isFalse();
    }
}
