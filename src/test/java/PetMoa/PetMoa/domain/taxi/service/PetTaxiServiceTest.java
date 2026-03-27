package PetMoa.PetMoa.domain.taxi.service;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class PetTaxiServiceTest {

    @Mock
    private PetTaxiRepository petTaxiRepository;

    @InjectMocks
    private PetTaxiService petTaxiService;

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
            PetTaxi result = petTaxiService.getPetTaxiById(1L);

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
            assertThatThrownBy(() -> petTaxiService.getPetTaxiById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("펫택시를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 이용 가능한 펫택시 조회")
        void getAvailableTaxis_Success() {
            // given
            given(petTaxiRepository.findByStatus(TaxiStatus.AVAILABLE)).willReturn(List.of(testTaxi));

            // when
            List<PetTaxi> result = petTaxiService.getAvailableTaxis();

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
            List<PetTaxi> result = petTaxiService.getAvailableTaxisForPetSize(PetSize.SMALL);

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
            List<PetTaxi> result = petTaxiService.getAvailableTaxisForPetSize(PetSize.LARGE);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVehicleSize()).isEqualTo(VehicleSize.LARGE);
        }
    }
}
