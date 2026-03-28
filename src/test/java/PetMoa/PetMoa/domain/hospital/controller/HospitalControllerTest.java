package PetMoa.PetMoa.domain.hospital.controller;

import PetMoa.PetMoa.domain.hospital.dto.HospitalSearchCondition;
import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.service.HospitalQueryService;
import PetMoa.PetMoa.domain.hospital.service.TimeSlotQueryService;
import PetMoa.PetMoa.domain.hospital.service.VeterinarianQueryService;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HospitalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private HospitalQueryService hospitalQueryService;

    @Mock
    private VeterinarianQueryService veterinarianQueryService;

    @Mock
    private TimeSlotQueryService timeSlotQueryService;

    @InjectMocks
    private HospitalController hospitalController;

    private Hospital testHospital;
    private Veterinarian testVet;
    private TimeSlot testTimeSlot;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(hospitalController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();

        testHospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .latitude(37.5)
                .longitude(127.0)
                .availablePetTypes(Set.of(PetType.DOG, PetType.CAT))
                .build();

        testVet = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.GENERAL)
                .hospital(testHospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();

        testTimeSlot = TimeSlot.builder()
                .veterinarian(testVet)
                .date(LocalDate.of(2024, 1, 15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .capacity(3)
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/hospitals")
    class GetHospitals {

        @Test
        @DisplayName("성공: 병원 목록 조회 (조건 없음)")
        void success() throws Exception {
            // given
            given(hospitalQueryService.searchWithConditions(any(HospitalSearchCondition.class)))
                    .willReturn(List.of(testHospital));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.hospitals").isArray())
                    .andExpect(jsonPath("$.result.hospitals[0].name").value("강남동물병원"))
                    .andExpect(jsonPath("$.result.hospitals[0].address").value("서울시 강남구 테헤란로 123"));
        }

        @Test
        @DisplayName("성공: 동물종류 + 위치 + 이름 복합 검색")
        void successSearchWithConditions() throws Exception {
            // given
            given(hospitalQueryService.searchWithConditions(any(HospitalSearchCondition.class)))
                    .willReturn(List.of(testHospital));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals")
                            .param("petType", "DOG")
                            .param("lat", "37.5")
                            .param("lng", "127.0")
                            .param("name", "강남"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.hospitals[0].name").value("강남동물병원"));
        }

        @Test
        @DisplayName("성공: 동물종류만 검색")
        void successSearchByPetType() throws Exception {
            // given
            given(hospitalQueryService.searchWithConditions(any(HospitalSearchCondition.class)))
                    .willReturn(List.of(testHospital));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals")
                            .param("petType", "DOG"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.hospitals[0].name").value("강남동물병원"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}")
    class GetHospitalDetail {

        @Test
        @DisplayName("성공: 병원 상세 조회")
        void success() throws Exception {
            // given
            given(hospitalQueryService.getHospitalById(1L)).willReturn(testHospital);
            given(veterinarianQueryService.getVeterinariansByHospitalId(1L)).willReturn(List.of(testVet));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.name").value("강남동물병원"))
                    .andExpect(jsonPath("$.result.veterinarians").isArray())
                    .andExpect(jsonPath("$.result.veterinarians[0].name").value("김수의"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 병원")
        void failNotFound() throws Exception {
            // given
            given(hospitalQueryService.getHospitalById(999L))
                    .willThrow(new EntityNotFoundException("병원을 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/veterinarians")
    class GetVeterinarians {

        @Test
        @DisplayName("성공: 수의사 목록 조회")
        void success() throws Exception {
            // given
            given(veterinarianQueryService.getVeterinariansByHospitalId(1L)).willReturn(List.of(testVet));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals/1/veterinarians"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.veterinarians").isArray())
                    .andExpect(jsonPath("$.result.veterinarians[0].name").value("김수의"))
                    .andExpect(jsonPath("$.result.veterinarians[0].department").value("GENERAL"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/hospitals/{hospitalId}/veterinarians/{vetId}/time-slots")
    class GetTimeSlots {

        @Test
        @DisplayName("성공: 수의사 예약 가능 시간 조회")
        void success() throws Exception {
            // given
            LocalDate date = LocalDate.of(2024, 1, 15);
            given(veterinarianQueryService.getVeterinarianById(1L)).willReturn(testVet);
            given(timeSlotQueryService.getTimeSlotsByVeterinarianAndDate(1L, date))
                    .willReturn(List.of(testTimeSlot));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals/1/veterinarians/1/time-slots")
                            .param("date", "2024-01-15"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.date").value("2024-01-15"))
                    .andExpect(jsonPath("$.result.veterinarian.name").value("김수의"))
                    .andExpect(jsonPath("$.result.timeSlots").isArray())
                    .andExpect(jsonPath("$.result.timeSlots[0].startTime").value("09:00:00"))
                    .andExpect(jsonPath("$.result.timeSlots[0].capacity").value(3))
                    .andExpect(jsonPath("$.result.timeSlots[0].available").value(true));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 수의사")
        void failVetNotFound() throws Exception {
            // given
            given(veterinarianQueryService.getVeterinarianById(999L))
                    .willThrow(new EntityNotFoundException("수의사를 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(get("/api/v1/hospitals/1/veterinarians/999/time-slots")
                            .param("date", "2024-01-15"))
                    .andExpect(status().isNotFound());
        }
    }
}
