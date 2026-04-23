package PetMoa.PetMoa.domain.hospital.entity;

import PetMoa.PetMoa.domain.pet.entity.PetType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Entity
@Table(name = "hospitals")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String address;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    private Double latitude;

    private Double longitude;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "hospital_available_pet_types", joinColumns = @JoinColumn(name = "hospital_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type")
    private Set<PetType> availablePetTypes = new HashSet<>();

    // Veterinarian은 Task #4에서 구현 예정
    @Transient
    private List<Object> veterinarians = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 전화번호 정규식: 02-1234-5678, 0212345678, 010-1234-5678 등
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{1,2}-?\\d{3,4}-?\\d{4}$");

    @Builder
    public Hospital(String name, String address, String phoneNumber, Double latitude, Double longitude, Set<PetType> availablePetTypes) {
        validateFields(name, address, phoneNumber, latitude, longitude);

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.availablePetTypes = (availablePetTypes != null) ? new HashSet<>(availablePetTypes) : new HashSet<>();
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String name, String address, String phoneNumber, Double latitude, Double longitude) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("병원 이름은 필수입니다.");
        }

        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소는 필수입니다.");
        }

        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("연락처는 필수입니다.");
        }

        if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 02-1234-5678)");
        }

        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new IllegalArgumentException("위도는 -90 ~ 90 사이의 값이어야 합니다.");
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new IllegalArgumentException("경도는 -180 ~ 180 사이의 값이어야 합니다.");
        }
    }

    // 비즈니스 메서드
    public void addAvailablePetType(PetType petType) {
        this.availablePetTypes.add(petType);
    }

    public boolean canTreat(PetType petType) {
        // availablePetTypes가 비어있으면 모든 동물 진료 가능
        if (this.availablePetTypes.isEmpty()) {
            return true;
        }
        return this.availablePetTypes.contains(petType);
    }
}
