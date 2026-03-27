package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.taxi.dto.TaxiAvailabilityResponse;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PetTaxiQueryServiceTest {

    @Mock
    private PetTaxiRepository petTaxiRepository;

    @InjectMocks
    private PetTaxiQueryService petTaxiQueryService;

    private PetTaxi testTaxi;

    @BeforeEach
    void setUp() {
        testTaxi = PetTaxi.builder()
                .licensePlate("서울12가3456")
                .driverName("홍길동")
                .driverPhoneNumber("010-1234-5678")
                .vehicleSize(VehicleSize.MEDIUM)
                .status(TaxiStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("펫택시 조회")
    class GetPetTaxi {

        @Test
        @DisplayName("성공: ID로 펫택시 조회")
        void getPetTaxiById_Success() {
            // given
            given(petTaxiRepository.findById(1L)).willReturn(Optional.of(testTaxi));

            // when
            PetTaxi result = petTaxiQueryService.getPetTaxiById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getDriverName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID")
        void getPetTaxiById_NotFound() {
            // given
            given(petTaxiRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> petTaxiQueryService.getPetTaxiById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("펫택시를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 이용 가능한 펫택시 조회")
        void getAvailableTaxis_Success() {
            // given
            given(petTaxiRepository.findByStatus(TaxiStatus.AVAILABLE)).willReturn(List.of(testTaxi));

            // when
            List<PetTaxi> result = petTaxiQueryService.getAvailableTaxis();

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).isAvailable()).isTrue();
        }

        @Test
        @DisplayName("성공: 반려동물 크기에 맞는 이용 가능한 펫택시 조회 (소형)")
        void getAvailableTaxisForPetSize_SmallPet() {
            // given
            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(testTaxi));

            // when
            List<PetTaxi> result = petTaxiQueryService.getAvailableTaxisForPetSize(PetSize.SMALL);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공: 대형견은 대형 차량만 가능")
        void getAvailableTaxisForPetSize_LargePet() {
            // given
            PetTaxi largeTaxi = PetTaxi.builder()
                    .licensePlate("서울34나5678")
                    .driverName("김철수")
                    .driverPhoneNumber("010-2222-3333")
                    .vehicleSize(VehicleSize.LARGE)
                    .status(TaxiStatus.AVAILABLE)
                    .build();

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(largeTaxi));

            // when
            List<PetTaxi> result = petTaxiQueryService.getAvailableTaxisForPetSize(PetSize.LARGE);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVehicleSize()).isEqualTo(VehicleSize.LARGE);
        }
    }

    @Nested
    @DisplayName("펫택시 이용 가능 여부 확인")
    class CheckAvailability {

        @Test
        @DisplayName("성공: 이용 가능한 택시가 있는 경우")
        void checkAvailability_Available() {
            // given
            PetSize petSize = PetSize.SMALL;
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(2);
            String pickupAddress = "서울시 강남구 역삼동 123";

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(testTaxi));

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.availableVehicleCount()).isEqualTo(1);
            assertThat(result.estimatedFee()).isNotNull();
            assertThat(result.estimatedArrivalMinutes()).isNotNull();
        }

        @Test
        @DisplayName("성공: 이용 가능한 택시가 없는 경우")
        void checkAvailability_NotAvailable() {
            // given
            PetSize petSize = PetSize.LARGE;
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(2);
            String pickupAddress = "서울시 강남구 역삼동 123";

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(Collections.emptyList());

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isFalse();
            assertThat(result.availableVehicleCount()).isEqualTo(0);
            assertThat(result.estimatedFee()).isNull();
        }

        @Test
        @DisplayName("성공: 여러 대의 택시가 가용한 경우")
        void checkAvailability_MultipleTaxis() {
            // given
            PetSize petSize = PetSize.MEDIUM;
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(2);
            String pickupAddress = "서울시 강남구 역삼동 123";

            PetTaxi taxi2 = PetTaxi.builder()
                    .licensePlate("서울34나5678")
                    .driverName("김철수")
                    .driverPhoneNumber("010-2222-3333")
                    .vehicleSize(VehicleSize.LARGE)
                    .status(TaxiStatus.AVAILABLE)
                    .build();

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.MEDIUM, VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(testTaxi, taxi2));

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.availableVehicleCount()).isEqualTo(2);
        }
    }
}
