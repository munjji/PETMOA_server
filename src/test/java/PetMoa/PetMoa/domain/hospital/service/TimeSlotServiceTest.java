package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.repository.TimeSlotRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock
    private TimeSlotRepository timeSlotRepository;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private Veterinarian testVet;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .build();

        testVet = Veterinarian.builder()
                .name("홍길동")
                .department(MedicalDepartment.GENERAL)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        testTimeSlot = TimeSlot.builder()
                .veterinarian(testVet)
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(3)
                .build();
    }

    @Nested
    @DisplayName("타임슬롯 조회")
    class GetTimeSlot {

        @Test
        @DisplayName("성공: ID로 타임슬롯 조회")
        void getTimeSlotById_Success() {
            // given
            given(timeSlotRepository.findById(1L)).willReturn(Optional.of(testTimeSlot));

            // when
            TimeSlot result = timeSlotService.getTimeSlotById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getStartTime()).isEqualTo(LocalTime.of(10, 0));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID")
        void getTimeSlotById_NotFound() {
            // given
            given(timeSlotRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> timeSlotService.getTimeSlotById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("타임슬롯을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 수의사와 날짜로 타임슬롯 조회")
        void getTimeSlotsByVeterinarianAndDate_Success() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            TimeSlot slot2 = TimeSlot.builder()
                    .veterinarian(testVet)
                    .date(date)
                    .startTime(LocalTime.of(11, 0))
                    .endTime(LocalTime.of(12, 0))
                    .capacity(3)
                    .build();
            given(timeSlotRepository.findByVeterinarianAndDate(1L, date)).willReturn(List.of(testTimeSlot, slot2));

            // when
            List<TimeSlot> result = timeSlotService.getTimeSlotsByVeterinarianAndDate(1L, date);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공: 예약 가능한 타임슬롯 조회")
        void getAvailableTimeSlots_Success() {
            // given
            LocalDate date = LocalDate.now().plusDays(1);
            given(timeSlotRepository.findAvailableSlots(1L, date)).willReturn(List.of(testTimeSlot));

            // when
            List<TimeSlot> result = timeSlotService.getAvailableTimeSlots(1L, date);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).hasAvailableSpace()).isTrue();
        }
    }
}
