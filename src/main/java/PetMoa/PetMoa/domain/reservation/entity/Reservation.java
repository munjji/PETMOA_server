package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @OneToOne(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HospitalReservation hospitalReservation;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaxiReservation> taxiReservations = new ArrayList<>();

    @OneToOne(mappedBy = "reservation", fetch = FetchType.LAZY)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Column(length = 500)
    private String memo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Reservation(User user, Pet pet, String memo) {
        validateFields(user, pet);

        this.user = user;
        this.pet = pet;
        this.memo = memo;
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(User user, Pet pet) {
        if (user == null) {
            throw new IllegalArgumentException("사용자는 필수입니다.");
        }

        if (pet == null) {
            throw new IllegalArgumentException("반려동물은 필수입니다.");
        }
    }

    // 비즈니스 메서드
    public void confirm() {
        if (!canConfirm()) {
            throw new IllegalStateException("대기 중인 예약만 확정할 수 있습니다.");
        }
        this.status = ReservationStatus.CONFIRMED;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public void complete() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 예약만 완료 처리할 수 있습니다.");
        }
        this.status = ReservationStatus.COMPLETED;
    }

    public void noShow() {
        if (this.status != ReservationStatus.CONFIRMED) {
            throw new IllegalStateException("확정된 예약만 노쇼 처리할 수 있습니다.");
        }
        this.status = ReservationStatus.NO_SHOW;
    }

    public boolean canConfirm() {
        return this.status == ReservationStatus.PENDING;
    }

    public boolean canCancel() {
        return this.status == ReservationStatus.PENDING || this.status == ReservationStatus.CONFIRMED;
    }

    public int calculateTotalAmount() {
        int total = 0;

        if (this.hospitalReservation != null) {
            total += this.hospitalReservation.getDepositAmount();
        }

        for (TaxiReservation taxi : this.taxiReservations) {
            if (taxi.getFare() != null) {
                total += taxi.getFare();
            }
        }

        return total;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
