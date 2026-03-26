package PetMoa.PetMoa.domain.reservation.entity;

import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.Veterinarian;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hospital_reservations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HospitalReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private Veterinarian veterinarian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false)
    private TimeSlot timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VisitType visitType;

    @Column(length = 500)
    private String symptoms;

    // MedicalRecord는 Task #11에서 구현 예정
    @Transient
    private List<Object> medicalRecords = new ArrayList<>();

    @Column(nullable = false)
    private Integer depositAmount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public HospitalReservation(Reservation reservation, Veterinarian veterinarian, TimeSlot timeSlot, VisitType visitType, String symptoms, Integer depositAmount) {
        validateFields(reservation, veterinarian, timeSlot, visitType, depositAmount);

        this.reservation = reservation;
        this.veterinarian = veterinarian;
        this.timeSlot = timeSlot;
        this.visitType = visitType;
        this.symptoms = symptoms;
        this.depositAmount = (depositAmount != null) ? depositAmount : 10000;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(Reservation reservation, Veterinarian veterinarian, TimeSlot timeSlot, VisitType visitType, Integer depositAmount) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }

        if (veterinarian == null) {
            throw new IllegalArgumentException("수의사는 필수입니다.");
        }

        if (timeSlot == null) {
            throw new IllegalArgumentException("타임슬롯은 필수입니다.");
        }

        if (visitType == null) {
            throw new IllegalArgumentException("진료 유형은 필수입니다.");
        }

        if (depositAmount != null && depositAmount <= 0) {
            throw new IllegalArgumentException("예약금은 0보다 커야 합니다.");
        }
    }

    // 비즈니스 메서드
    public boolean isFirstVisit() {
        return this.visitType == VisitType.FIRST_VISIT;
    }
}
