package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.repository.HospitalRepository;
import PetMoa.PetMoa.domain.pet.entity.PetType;
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
class HospitalServiceTest {

    @Mock
    private HospitalRepository hospitalRepository;

    @InjectMocks
    private HospitalService hospitalService;

    private Hospital testHospital;

    @BeforeEach
    void setUp() {
        testHospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .latitude(37.5)
                .longitude(127.0)
                .availablePetTypes(Set.of(PetType.DOG, PetType.CAT))
                .build();
    }

    @Nested
    @DisplayName("병원 조회")
    class GetHospital {

        @Test
        @DisplayName("성공: ID로 병원 조회")
        void getHospitalById_Success() {
            // given
            given(hospitalRepository.findById(1L)).willReturn(Optional.of(testHospital));

            // when
            Hospital result = hospitalService.getHospitalById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("강남동물병원");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID")
        void getHospitalById_NotFound() {
            // given
            given(hospitalRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> hospitalService.getHospitalById(999L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("병원을 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 전체 병원 조회")
        void getAllHospitals_Success() {
            // given
            Hospital hospital2 = Hospital.builder()
                    .name("서초동물병원")
                    .address("서울시 서초구")
                    .phoneNumber("02-2345-6789")
                    .build();
            given(hospitalRepository.findAll()).willReturn(List.of(testHospital, hospital2));

            // when
            List<Hospital> result = hospitalService.getAllHospitals();

            // then
            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("병원 검색")
    class SearchHospital {

        @Test
        @DisplayName("성공: 이름으로 병원 검색")
        void searchByName_Success() {
            // given
            given(hospitalRepository.findByNameContaining("강남")).willReturn(List.of(testHospital));

            // when
            List<Hospital> result = hospitalService.searchByName("강남");

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).contains("강남");
        }

        @Test
        @DisplayName("성공: 주소로 병원 검색")
        void searchByAddress_Success() {
            // given
            given(hospitalRepository.findByAddressContaining("강남구")).willReturn(List.of(testHospital));

            // when
            List<Hospital> result = hospitalService.searchByAddress("강남구");

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공: 진료 가능 동물 타입으로 병원 검색")
        void searchByPetType_Success() {
            // given
            given(hospitalRepository.findByAvailablePetType(PetType.DOG)).willReturn(List.of(testHospital));

            // when
            List<Hospital> result = hospitalService.searchByPetType(PetType.DOG);

            // then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("성공: 근처 병원 검색")
        void searchNearby_Success() {
            // given
            given(hospitalRepository.findNearbyHospitals(37.5, 127.0, 5.0)).willReturn(List.of(testHospital));

            // when
            List<Hospital> result = hospitalService.searchNearby(37.5, 127.0, 5.0);

            // then
            assertThat(result).hasSize(1);
        }
    }
}
