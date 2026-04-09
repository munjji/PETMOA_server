package PetMoa.PetMoa.domain.reservation.service;

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
import PetMoa.PetMoa.domain.reservation.dto.HospitalReservationRequest;
import PetMoa.PetMoa.domain.reservation.dto.ReservationCreateRequest;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.reservation.repository.HospitalReservationRepository;
import PetMoa.PetMoa.domain.reservation.repository.ReservationRepository;
import PetMoa.PetMoa.domain.reservation.repository.TaxiReservationRepository;
import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import PetMoa.PetMoa.domain.taxi.entity.TaxiStatus;
import PetMoa.PetMoa.domain.taxi.entity.VehicleSize;
import PetMoa.PetMoa.domain.taxi.repository.PetTaxiRepository;
import PetMoa.PetMoa.domain.user.entity.User;
import PetMoa.PetMoa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class ReservationConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(ReservationConcurrencyTest.class);

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("petmoa_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ReservationFacade reservationFacade;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private VeterinarianRepository veterinarianRepository;

    @Autowired
    private TimeSlotRepository timeSlotRepository;

    @Autowired
    private PetTaxiRepository petTaxiRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private HospitalReservationRepository hospitalReservationRepository;

    @Autowired
    private TaxiReservationRepository taxiReservationRepository;

    private TimeSlot timeSlot;
    private List<User> users;
    private List<Pet> pets;

    @BeforeEach
    void setUp() {
        // FK 제약조건 순서대로 삭제
        taxiReservationRepository.deleteAll();
        hospitalReservationRepository.deleteAll();
        reservationRepository.deleteAll();
        timeSlotRepository.deleteAll();
        veterinarianRepository.deleteAll();
        hospitalRepository.deleteAll();
        petRepository.deleteAll();
        userRepository.deleteAll();
        petTaxiRepository.deleteAll();

        // Hospital
        Hospital hospital = hospitalRepository.save(Hospital.builder()
                .name("강남동물병원")
                .address("서울시 강남구")
                .phoneNumber("02-1234-5678")
                .build());

        // Veterinarian
        Veterinarian vet = veterinarianRepository.save(Veterinarian.builder()
                .name("김수의")
                .department(MedicalDepartment.GENERAL)
                .hospital(hospital)
                .workStartTime(LocalTime.of(9, 0))
                .workEndTime(LocalTime.of(18, 0))
                .build());

        // TimeSlot - 정원 3명
        timeSlot = timeSlotRepository.save(TimeSlot.builder()
                .veterinarian(vet)
                .date(LocalDate.now().plusDays(7))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .capacity(3)
                .build());

        // PetTaxi
        petTaxiRepository.save(PetTaxi.builder()
                .licensePlate("서울12가3456")
                .driverName("박기사")
                .driverPhoneNumber("010-9999-8888")
                .vehicleSize(VehicleSize.LARGE)
                .status(TaxiStatus.AVAILABLE)
                .build());

        // 100명의 User와 Pet 생성
        users = new ArrayList<>();
        pets = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            User user = userRepository.save(User.builder()
                    .name("사용자" + i)
                    .phoneNumber("010-0000-" + String.format("%04d", i))
                    .address("서울시 강남구")
                    .build());
            users.add(user);

            Pet pet = petRepository.save(Pet.builder()
                    .name("반려동물" + i)
                    .type(PetType.DOG)
                    .size(PetSize.SMALL)
                    .owner(user)
                    .build());
            pets.add(pet);
        }
    }

    @Test
    @DisplayName("동시성 테스트: 100명 동시 요청 시 정원(3명)만큼만 성공")
    void concurrentReservation_onlyCapacitySucceeds() throws InterruptedException {
        // given
        int threadCount = 100;
        int capacity = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<String> failReasons = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    ReservationCreateRequest request = new ReservationCreateRequest(
                            pets.get(index).getId(),
                            new HospitalReservationRequest(timeSlot.getId(), "증상" + index),
                            null
                    );
                    Reservation result = reservationFacade.createReservation(users.get(index).getId(), request);
                    if (result != null) {
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    failReasons.add(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        assertThat(successCount.get()).isEqualTo(capacity);
        assertThat(failCount.get()).isEqualTo(threadCount - capacity);

        // DB 상태 확인
        TimeSlot updatedTimeSlot = timeSlotRepository.findById(timeSlot.getId()).orElseThrow();
        assertThat(updatedTimeSlot.getCurrentReservations()).isEqualTo(capacity);
        assertThat(updatedTimeSlot.hasAvailableSpace()).isFalse();

        System.out.println("=== 동시성 테스트 결과 ===");
        System.out.println("성공: " + successCount.get());
        System.out.println("실패: " + failCount.get());
        System.out.println("실패 사유(일부): " + failReasons.stream().limit(5).toList());
    }

    @Test
    @DisplayName("동시성 테스트: 락 획득 실패 시에도 데이터 정합성 유지")
    void concurrentReservation_dataIntegrityMaintained() throws InterruptedException {
        // given
        int threadCount = 50;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    ReservationCreateRequest request = new ReservationCreateRequest(
                            pets.get(index).getId(),
                            new HospitalReservationRequest(timeSlot.getId(), "증상" + index),
                            null
                    );
                    reservationFacade.createReservation(users.get(index).getId(), request);
                } catch (Exception e) {
                    log.debug("예약 실패 - index: {}, 사유: {}", index, e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        TimeSlot updatedTimeSlot = timeSlotRepository.findById(timeSlot.getId()).orElseThrow();
        long actualReservationCount = reservationRepository.count();

        // 정원을 초과하지 않음
        assertThat(updatedTimeSlot.getCurrentReservations()).isLessThanOrEqualTo(3);
        // DB의 예약 수와 TimeSlot의 currentReservations 일치
        assertThat(actualReservationCount).isEqualTo((long) updatedTimeSlot.getCurrentReservations());
    }
}
