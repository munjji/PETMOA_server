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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaxiReservationStatus status;

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
        this.status = TaxiReservationStatus.ASSIGNED;
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

    public void pickUp() {
        if (this.status != TaxiReservationStatus.ASSIGNED) {
            throw new IllegalStateException("배차 완료 상태에서만 픽업할 수 있습니다.");
        }
        this.status = TaxiReservationStatus.PICKED_UP;
    }

    public void startRide() {
        if (this.status != TaxiReservationStatus.PICKED_UP) {
            throw new IllegalStateException("픽업 완료 상태에서만 운행을 시작할 수 있습니다.");
        }
        this.status = TaxiReservationStatus.IN_PROGRESS;
    }

    public void complete() {
        if (this.status != TaxiReservationStatus.IN_PROGRESS) {
            throw new IllegalStateException("운행 중 상태에서만 완료 처리할 수 있습니다.");
        }
        this.status = TaxiReservationStatus.COMPLETED;
    }

    public void cancel() {
        if (this.status == TaxiReservationStatus.COMPLETED || this.status == TaxiReservationStatus.CANCELLED) {
            throw new IllegalStateException("이미 완료되었거나 취소된 배차입니다.");
        }
        this.status = TaxiReservationStatus.CANCELLED;
    }
}
