package PetMoa.PetMoa.domain.hospital.entity;

import PetMoa.PetMoa.domain.pet.entity.Pet;
import PetMoa.PetMoa.domain.reservation.entity.HospitalReservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "medical_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_reservation_id", nullable = false)
    private HospitalReservation hospitalReservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 200)
    private String diagnosis;

    @Column(length = 1000)
    private String treatment;

    @Column(length = 500)
    private String prescription;

    private Integer treatmentCost;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime visitDate;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public MedicalRecord(HospitalReservation hospitalReservation, Pet pet, String diagnosis, String treatment, String prescription, Integer treatmentCost, String notes, LocalDateTime visitDate) {
        validateFields(hospitalReservation, pet, diagnosis, visitDate, treatmentCost);

        this.hospitalReservation = hospitalReservation;
        this.pet = pet;
        this.diagnosis = diagnosis;
        this.treatment = treatment;
        this.prescription = prescription;
        this.treatmentCost = treatmentCost;
        this.notes = notes;
        this.visitDate = visitDate;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(HospitalReservation hospitalReservation, Pet pet, String diagnosis, LocalDateTime visitDate, Integer treatmentCost) {
        if (hospitalReservation == null) {
            throw new IllegalArgumentException("병원 예약은 필수입니다.");
        }

        if (pet == null) {
            throw new IllegalArgumentException("반려동물은 필수입니다.");
        }

        if (diagnosis == null || diagnosis.isBlank()) {
            throw new IllegalArgumentException("진단명은 필수입니다.");
        }

        if (visitDate == null) {
            throw new IllegalArgumentException("진료 일시는 필수입니다.");
        }

        if (treatmentCost != null && treatmentCost <= 0) {
            throw new IllegalArgumentException("진료비는 0보다 커야 합니다.");
        }
    }

    // 비즈니스 메서드
    public String getSummary() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        StringBuilder summary = new StringBuilder();
        summary.append("[").append(visitDate.format(formatter)).append("] ");
        summary.append("진단: ").append(diagnosis);
        if (treatment != null && !treatment.isBlank()) {
            summary.append(" | 처치: ").append(treatment);
        }
        return summary.toString();
    }

    public boolean hasPrescription() {
        return this.prescription != null && !this.prescription.isBlank();
    }
}
