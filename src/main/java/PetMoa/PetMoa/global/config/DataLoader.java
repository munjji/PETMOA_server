package PetMoa.PetMoa.global.config;

import PetMoa.PetMoa.domain.hospital.entity.Hospital;
import PetMoa.PetMoa.domain.hospital.entity.MedicalDepartment;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import PetMoa.PetMoa.domain.hospital.repository.HospitalRepository;
import PetMoa.PetMoa.domain.hospital.repository.TimeSlotRepository;
import PetMoa.PetMoa.domain.hospital.repository.VeterinarianRepository;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.pet.entity.PetSize;
import PetMoa.PetMoa.domain.pet.entity.PetType;
import PetMoa.PetMoa.domain.pet.repository.PetRepository;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PetRepository petRepository;
    private final HospitalRepository hospitalRepository;
    private final VeterinarianRepository veterinarianRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final PetTaxiRepository petTaxiRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("데이터가 이미 존재합니다. DataLoader를 건너뜁니다.");
            return;
        }

        log.info("테스트 데이터 생성 시작...");

        // 1. 사용자 생성
        User user = User.builder()
                .name("홍길동")
                .email("hong@test.com")
                .phoneNumber("010-1234-5678")
                .address("서울시 강남구 역삼동")
                .build();
        userRepository.save(user);

        // 2. 반려동물 생성
        Pet pet = Pet.builder()
                .owner(user)
                .name("뽀삐")
                .type(PetType.DOG)
                .size(PetSize.SMALL)
                .breed("말티즈")
                .age(3)
                .weight(4.5)
                .build();
        petRepository.save(pet);

        // 3. 병원 생성
        Hospital hospital = Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구 테헤란로 123")
                .phoneNumber("02-1234-5678")
                .latitude(37.5)
                .longitude(127.0)
                .availablePetTypes(Set.of(PetType.DOG, PetType.CAT))
                .build();
        hospitalRepository.save(hospital);

        // 4. 수의사 생성
        Veterinarian vet = Veterinarian.builder()
                .hospital(hospital)
                .name("김수의")
                .department(MedicalDepartment.GENERAL)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build();
        veterinarianRepository.save(vet);

        // 5. 타임슬롯 생성 (오늘 + 내일)
        for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
            LocalDate date = LocalDate.now().plusDays(dayOffset);
            for (int hour = 9; hour < 18; hour++) {
                TimeSlot timeSlot = TimeSlot.builder()
                        .veterinarian(vet)
                        .date(date)
                        .startTime(LocalTime.of(hour, 0))
                        .endTime(LocalTime.of(hour + 1, 0))
                        .capacity(3)
                        .build();
                timeSlotRepository.save(timeSlot);
            }
        }

        // 6. 펫택시 생성
        PetTaxi taxi1 = PetTaxi.builder()
                .licensePlate("서울12가3456")
                .driverName("김기사")
                .driverPhoneNumber("010-1111-2222")
                .vehicleSize(VehicleSize.MEDIUM)
                .status(TaxiStatus.AVAILABLE)
                .build();
        petTaxiRepository.save(taxi1);

        PetTaxi taxi2 = PetTaxi.builder()
                .licensePlate("서울34나5678")
                .driverName("이기사")
                .driverPhoneNumber("010-3333-4444")
                .vehicleSize(VehicleSize.LARGE)
                .status(TaxiStatus.AVAILABLE)
                .build();
        petTaxiRepository.save(taxi2);

        log.info("테스트 데이터 생성 완료!");
        log.info("- 사용자: {} (ID: {})", user.getName(), user.getId());
        log.info("- 반려동물: {} (ID: {})", pet.getName(), pet.getId());
        log.info("- 병원: {} (ID: {})", hospital.getName(), hospital.getId());
        log.info("- 수의사: {} (ID: {})", vet.getName(), vet.getId());
        log.info("- 타임슬롯: 27개 생성");
        log.info("- 펫택시: 2대 생성");
    }
}