package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.taxi.entity.PetTaxi;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "taxi_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaxiReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "taxi_id", nullable = false)
    private PetTaxi taxi;

    @Column(nullable = false, length = 200)
    private String pickupAddress;

    @Column(nullable = false, length = 200)
    private String dropoffAddress;

    @Column(nullable = false)
    private LocalDateTime pickupTime;

    private Double distance;

    private Integer fare;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TaxiReservationType type;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 기본 요금: 5,000원, km당 요금: 1,000원
    private static final int BASE_FARE = 5000;
    private static final int FARE_PER_KM = 1000;

    @Builder
    public TaxiReservation(Reservation reservation, PetTaxi taxi, String pickupAddress, String dropoffAddress, LocalDateTime pickupTime, Double distance, Integer fare, TaxiReservationType type) {
        validateFields(reservation, taxi, pickupAddress, dropoffAddress, pickupTime, distance, fare);

        this.reservation = reservation;
        this.taxi = taxi;
        this.pickupAddress = pickupAddress;
        this.dropoffAddress = dropoffAddress;
        this.pickupTime = pickupTime;
        this.distance = distance;
        this.fare = (fare != null) ? fare : calculateFare(distance);
        this.type = type;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(Reservation reservation, PetTaxi taxi, String pickupAddress, String dropoffAddress, LocalDateTime pickupTime, Double distance, Integer fare) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }

        if (taxi == null) {
            throw new IllegalArgumentException("택시는 필수입니다.");
        }

        if (pickupAddress == null || pickupAddress.isBlank()) {
            throw new IllegalArgumentException("픽업 주소는 필수입니다.");
        }

        if (dropoffAddress == null || dropoffAddress.isBlank()) {
            throw new IllegalArgumentException("목적지 주소는 필수입니다.");
        }

        if (pickupTime == null) {
            throw new IllegalArgumentException("픽업 시간은 필수입니다.");
        }

        if (distance != null && distance <= 0) {
            throw new IllegalArgumentException("거리는 0보다 커야 합니다.");
        }

        if (fare != null && fare <= 0) {
            throw new IllegalArgumentException("요금은 0보다 커야 합니다.");
        }
    }

    private Integer calculateFare(Double distance) {
        if (distance == null) {
            return null;
        }
        return BASE_FARE + (int) (distance * FARE_PER_KM);
    }

    // 비즈니스 메서드
    public boolean isPickup() {
        return this.type == TaxiReservationType.PICKUP;
    }

    public boolean isReturn() {
        return this.type == TaxiReservationType.RETURN;
    }
}
