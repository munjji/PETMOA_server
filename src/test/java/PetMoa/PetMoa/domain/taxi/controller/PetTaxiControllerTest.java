package PetMoa.PetMoa.domain.taxi.controller;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.taxi.dto.TaxiAvailabilityResponse;
import PetMoa.PetMoa.domain.taxi.service.PetTaxiQueryService;
import PetMoa.PetMoa.global.apiPayload.exception.ExceptionAdvice;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PetTaxiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PetTaxiQueryService petTaxiQueryService;

    @InjectMocks
    private PetTaxiController petTaxiController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(petTaxiController)
                .setControllerAdvice(new ExceptionAdvice())
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/pet-taxis/check-availability")
    class CheckAvailability {

        @Test
        @DisplayName("성공: 이용 가능한 택시 있음")
        void successAvailable() throws Exception {
            // given
            TaxiAvailabilityResponse response = TaxiAvailabilityResponse.of(3, 15000, 10);

            given(petTaxiQueryService.checkAvailability(
                    eq(PetSize.SMALL),
                    any(LocalDateTime.class),
                    eq("서울시 강남구 역삼동 123")
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/pet-taxis/check-availability")
                            .param("petSize", "SMALL")
                            .param("pickupTime", "2024-01-15T08:30:00")
                            .param("pickupAddress", "서울시 강남구 역삼동 123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.available").value(true))
                    .andExpect(jsonPath("$.result.estimatedFee").value(15000))
                    .andExpect(jsonPath("$.result.estimatedArrivalMinutes").value(10))
                    .andExpect(jsonPath("$.result.availableVehicleCount").value(3));
        }

        @Test
        @DisplayName("성공: 이용 가능한 택시 없음")
        void successNotAvailable() throws Exception {
            // given
            TaxiAvailabilityResponse response = TaxiAvailabilityResponse.notAvailable();

            given(petTaxiQueryService.checkAvailability(
                    eq(PetSize.LARGE),
                    any(LocalDateTime.class),
                    eq("서울시 강남구 역삼동 123")
            )).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/v1/pet-taxis/check-availability")
                            .param("petSize", "LARGE")
                            .param("pickupTime", "2024-01-15T08:30:00")
                            .param("pickupAddress", "서울시 강남구 역삼동 123"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isSuccess").value(true))
                    .andExpect(jsonPath("$.result.available").value(false))
                    .andExpect(jsonPath("$.result.availableVehicleCount").value(0));
        }

        @Test
        @DisplayName("실패: 필수 파라미터 누락")
        void failMissingParameter() throws Exception {
            mockMvc.perform(get("/api/v1/pet-taxis/check-availability")
                            .param("petSize", "SMALL"))
                    .andExpect(status().isBadRequest());
        }
    }
}
