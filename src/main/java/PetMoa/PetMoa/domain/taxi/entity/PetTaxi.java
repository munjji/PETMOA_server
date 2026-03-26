package PetMoa.PetMoa.domain.taxi.entity;

import PetMoa.PetMoa.domain.pet.entity.PetSize;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Entity
@Table(name = "pet_taxis")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PetTaxi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String licensePlate;

    @Column(nullable = false, length = 50)
    private String driverName;

    @Column(nullable = false, length = 20)
    private String driverPhoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleSize vehicleSize;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaxiStatus status;

    // TaxiReservation은 Task #9에서 구현 예정
    @Transient
    private List<Object> reservations = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 전화번호 정규식: 010-1234-5678 또는 01012345678
    private static final Pattern PHONE_PATTERN = Pattern.compile("^01(?:0|1|[6-9])-?(?:\\d{3}|\\d{4})-?\\d{4}$");

    @Builder
    public PetTaxi(String licensePlate, String driverName, String driverPhoneNumber, VehicleSize vehicleSize, TaxiStatus status) {
        validateFields(licensePlate, driverName, driverPhoneNumber, vehicleSize);

        this.licensePlate = licensePlate;
        this.driverName = driverName;
        this.driverPhoneNumber = driverPhoneNumber;
        this.vehicleSize = vehicleSize;
        this.status = (status != null) ? status : TaxiStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String licensePlate, String driverName, String driverPhoneNumber, VehicleSize vehicleSize) {
        if (licensePlate == null || licensePlate.trim().isEmpty()) {
            throw new IllegalArgumentException("차량 번호는 필수입니다.");
        }

        if (driverName == null || driverName.trim().isEmpty()) {
            throw new IllegalArgumentException("기사 이름은 필수입니다.");
        }

        if (driverPhoneNumber == null || driverPhoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("기사 연락처는 필수입니다.");
        }

        if (!PHONE_PATTERN.matcher(driverPhoneNumber).matches()) {
            throw new IllegalArgumentException("전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678 또는 01012345678)");
        }

        if (vehicleSize == null) {
            throw new IllegalArgumentException("차량 크기는 필수입니다.");
        }
    }

    // 비즈니스 메서드
    public boolean canAccommodate(PetSize petSize) {
        return switch (this.vehicleSize) {
            case SMALL -> petSize == PetSize.SMALL;
            case MEDIUM -> petSize == PetSize.SMALL || petSize == PetSize.MEDIUM;
            case LARGE -> true; // 모든 크기 가능
        };
    }

    public boolean isAvailable() {
        return this.status == TaxiStatus.AVAILABLE;
    }

    public void changeStatus(TaxiStatus newStatus) {
        this.status = newStatus;
    }
}
