package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.reservation.repository.TaxiReservationRepository;
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

import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private TaxiReservationRepository taxiReservationRepository;

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
            given(taxiReservationRepository.findReservedTaxiIdsBetween(any(), any()))
                    .willReturn(Collections.emptySet());

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.availableVehicleCount()).isEqualTo(1);
            assertThat(result.estimatedFee()).isNotNull();
            assertThat(result.estimatedArrivalMinutes()).isNotNull();
        }

        @Test
        @DisplayName("성공: 이용 가능한 택시가 없는 경우 (AVAILABLE 상태인 택시 없음)")
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
            given(taxiReservationRepository.findReservedTaxiIdsBetween(any(), any()))
                    .willReturn(Collections.emptySet());

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isTrue();
            assertThat(result.availableVehicleCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("성공: 해당 시간대에 이미 예약된 택시는 제외")
        void checkAvailability_ExcludeReservedTaxis() {
            // given
            PetSize petSize = PetSize.SMALL;
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(2);
            String pickupAddress = "서울시 강남구 역삼동 123";

            // ID 설정
            ReflectionTestUtils.setField(testTaxi, "id", 1L);

            PetTaxi taxi2 = PetTaxi.builder()
                    .licensePlate("서울34나5678")
                    .driverName("김철수")
                    .driverPhoneNumber("010-2222-3333")
                    .vehicleSize(VehicleSize.MEDIUM)
                    .status(TaxiStatus.AVAILABLE)
                    .build();
            ReflectionTestUtils.setField(taxi2, "id", 2L);

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(testTaxi, taxi2));
            // testTaxi(ID=1)가 해당 시간대에 이미 예약됨
            given(taxiReservationRepository.findReservedTaxiIdsBetween(any(), any()))
                    .willReturn(Set.of(1L));

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            // 2대 중 1대(ID=1)가 예약되어 있으므로 실제 가용 차량은 1대
            assertThat(result.available()).isTrue();
            assertThat(result.availableVehicleCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패: 모든 택시가 해당 시간대에 예약된 경우")
        void checkAvailability_AllTaxisReserved() {
            // given
            PetSize petSize = PetSize.SMALL;
            LocalDateTime pickupTime = LocalDateTime.now().plusHours(2);
            String pickupAddress = "서울시 강남구 역삼동 123";

            // ID 설정
            ReflectionTestUtils.setField(testTaxi, "id", 1L);

            Set<VehicleSize> allowedSizes = Set.of(VehicleSize.SMALL, VehicleSize.MEDIUM, VehicleSize.LARGE);
            given(petTaxiRepository.findByStatusAndVehicleSizeIn(TaxiStatus.AVAILABLE, allowedSizes))
                    .willReturn(List.of(testTaxi));
            // testTaxi(ID=1)가 해당 시간대에 이미 예약됨
            given(taxiReservationRepository.findReservedTaxiIdsBetween(any(), any()))
                    .willReturn(Set.of(1L));

            // when
            TaxiAvailabilityResponse result = petTaxiQueryService.checkAvailability(petSize, pickupTime, pickupAddress);

            // then
            assertThat(result.available()).isFalse();
            assertThat(result.availableVehicleCount()).isEqualTo(0);
        }
    }
}
