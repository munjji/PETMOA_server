package PetMoa.PetMoa.domain.hospital.service;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.repository.VeterinarianRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class VeterinarianServiceTest {

    @Mock
    private VeterinarianRepository veterinarianRepository;

    @InjectMocks
    private VeterinarianService veterinarianService;

    private Hospital testHospital;
    private Veterinarian testVet;

    @BeforeEach
    void setUp() {
        testHospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .build();

        testVet = Veterinarian.builder()
                .name("홍길동")
                .department(MedicalDepartment.GENERAL)
                .hospital(testHospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();
    }

    @Nested
    @DisplayName("수의사 조회")
    class GetVeterinarian {

        @Test
        @DisplayName("성공: ID로 수의사 조회")
        void getVeterinarianById_Success() {
            // given
            given(veterinarianRepository.findById(1L)).willReturn(Optional.of(testVet));

            // when
            Veterinarian result = veterinarianService.getVeterinarianById(1L);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("홍길동");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID")
        void getVeterinarianById_NotFound() {
            // given
            given(veterinarianRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> veterinarianService.getVeterinarianById(999L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("수의사를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("성공: 병원 ID로 수의사 목록 조회")
        void getVeterinariansByHospitalId_Success() {
            // given
            Veterinarian vet2 = Veterinarian.builder()
                    .name("김철수")
                    .department(MedicalDepartment.SURGERY)
                    .hospital(testHospital)
                    .workStartTime(LocalTime.of(10, 0))
                    .workEndTime(LocalTime.of(19, 0))
                    .build();
            given(veterinarianRepository.findByHospitalId(1L)).willReturn(List.of(testVet, vet2));

            // when
            List<Veterinarian> result = veterinarianService.getVeterinariansByHospitalId(1L);

            // then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("성공: 진료과목으로 수의사 목록 조회")
        void getVeterinariansByDepartment_Success() {
            // given
            given(veterinarianRepository.findByDepartment(MedicalDepartment.GENERAL)).willReturn(List.of(testVet));

            // when
            List<Veterinarian> result = veterinarianService.getVeterinariansByDepartment(MedicalDepartment.GENERAL);

            // then
            assertThat(result).hasSize(1);
        }
    }
}
