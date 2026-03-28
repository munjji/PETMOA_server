package PetMoa.PetMoa.domain.reservation.controller;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.reservation.dto.HospitalReservationRequest;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.dto.TaxiRequest;
import PetMoa.PetMoa.domain.reservation.entity.*;
import PetMoa.PetMoa.domain.reservation.service.ReservationCommandService;
import PetMoa.PetMoa.domain.reservation.service.ReservationQueryService;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReservationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReservationQueryService reservationQueryService;

    @Mock
    private ReservationCommandService reservationCommandService;

    @InjectMocks
    private ReservationController reservationController;

    private User testUser;
    private Pet testPet;
    private Hospital testHospital;
    private Veterinarian testVet;
    private TimeSlot testTimeSlot;
    private PetTaxi testTaxi;
    private Reservation testReservation;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reservationController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // User
        testUser = User.builder()
                .name("홍길동")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구")
                .build();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // Pet
        testPet = Pet.builder()
                .name("뽀삐")
                .type(PetType.DOG)
                .size(PetSize.SMALL)
                .owner(testUser)
                .build();
        ReflectionTestUtils.setField(testPet, "id", 1L);

        // Hospital
        testHospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .build();
        ReflectionTestUtils.setField(testHospital, "id", 1L);

        // Veterinarian
        testVet = Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.GENERAL)
                .hospital(testHospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();
        ReflectionTestUtils.setField(testVet, "id", 1L);

        // TimeSlot
        testTimeSlot = TimeSlot.builder()
                .veterinarian(testVet)
                .date(LocalDate.of(2024, 1, 15))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .capacity(3)
                .build();
        ReflectionTestUtils.setField(testTimeSlot, "id", 1L);

        // PetTaxi
        testTaxi = PetTaxi.builder()
                .licensePlate("서울12가3456")
                .driverName("박기사")
                .driverPhoneNumber("010-9999-8888")
                .vehicleSize(VehicleSize.MEDIUM)
                .status(TaxiStatus.AVAILABLE)
                .build();
        ReflectionTestUtils.setField(testTaxi, "id", 1L);

        // Reservation
        testReservation = Reservation.builder()
                .user(testUser)
                .pet(testPet)
                .build();
        ReflectionTestUtils.setField(testReservation, "id", 1L);

        // HospitalReservation
        HospitalReservation hospitalReservation = HospitalReservation.builder()
                .reservation(testReservation)
                .veterinarian(testVet)
                .timeSlot(testTimeSlot)
                .visitType(VisitType.FIRST_VISIT)
                .symptoms("기침을 자주 해요")
                .depositAmount(10000)
                .build();
        ReflectionTestUtils.setField(testReservation, "hospitalReservation", hospitalReservation);
    }

    @Nested
    @DisplayName("POST /api/v1/reservations")
    class CreateReservation {

        @Test
        @DisplayName("성공: 병원 예약만 생성")
        void successHospitalOnly() throws Exception {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    1L,
                    new HospitalReservationRequest(1L, "기침을 자주 해요"),
                    null
            );

            given(reservationCommandService.createReservation(eq(1L), any(ReservationCreateRequest.class)))
                    .willReturn(testReservation);

            // when & then
            mockMvc.perform(post("/api/v1/reservations")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.status").value("PENDING"))
                    .andExpect(jsonPath("$.result.hospitalReservation.hospitalName").value("강남동물병원"));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 타임슬롯")
        void failTimeSlotNotFound() throws Exception {
            // given
            ReservationCreateRequest request = new ReservationCreateRequest(
                    1L,
                    new HospitalReservationRequest(999L, "기침을 자주 해요"),
                    null
            );

            given(reservationCommandService.createReservation(eq(1L), any(ReservationCreateRequest.class)))
                    .willThrow(new EntityNotFoundException("타임슬롯을 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/v1/reservations")
                            .header("X-User-Id", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reservations")
    class GetMyReservations {

        @Test
        @DisplayName("성공: 내 예약 목록 조회")
        void success() throws Exception {
            // given
            given(reservationQueryService.getReservationsByUserId(1L))
                    .willReturn(List.of(testReservation));

            // when & then
            mockMvc.perform(get("/api/v1/reservations")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reservations").isArray())
                    .andExpect(jsonPath("$.result.reservations[0].id").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/reservations/{reservationId}")
    class GetReservation {

        @Test
        @DisplayName("성공: 예약 상세 조회")
        void success() throws Exception {
            // given
            given(reservationQueryService.getReservationByIdAndUserId(1L, 1L))
                    .willReturn(testReservation);

            // when & then
            mockMvc.perform(get("/api/v1/reservations/1")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.id").value(1))
                    .andExpect(jsonPath("$.result.pet.name").value("뽀삐"));
        }

        @Test
        @DisplayName("실패: 다른 사용자의 예약 조회")
        void failNotOwner() throws Exception {
            // given
            given(reservationQueryService.getReservationByIdAndUserId(1L, 2L))
                    .willThrow(new IllegalArgumentException("해당 예약의 소유자가 아닙니다."));

            // when & then
            mockMvc.perform(get("/api/v1/reservations/1")
                            .header("X-User-Id", "2"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/reservations/{reservationId}/cancel")
    class CancelReservation {

        @Test
        @DisplayName("성공: 예약 취소 (24시간 전)")
        void success() throws Exception {
            // given
            testReservation.cancel();
            given(reservationCommandService.cancelReservation(1L, 1L))
                    .willReturn(testReservation);
            given(reservationCommandService.calculateRefundRate(testReservation))
                    .willReturn(100);

            // when & then
            mockMvc.perform(post("/api/v1/reservations/1/cancel")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.reservationId").value(1))
                    .andExpect(jsonPath("$.result.status").value("CANCELLED"))
                    .andExpect(jsonPath("$.result.refund.refundRate").value(100));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 예약")
        void failNotFound() throws Exception {
            // given
            given(reservationCommandService.cancelReservation(1L, 999L))
                    .willThrow(new EntityNotFoundException("예약을 찾을 수 없습니다."));

            // when & then
            mockMvc.perform(post("/api/v1/reservations/999/cancel")
                            .header("X-User-Id", "1"))
                    .andExpect(status().isNotFound());
        }
    }
}
