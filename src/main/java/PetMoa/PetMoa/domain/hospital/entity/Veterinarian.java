package PetMoa.PetMoa.domain.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veterinarians")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Veterinarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MedicalDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    @Column(nullable = false)
    private LocalTime workStartTime;

    @Column(nullable = false)
    private LocalTime workEndTime;

    // TimeSlot은 Task #5에서 구현 예정
    @Transient
    private List<Object> timeSlots = new ArrayList<>();

    // HospitalReservation은 Task #8에서 구현 예정
    @Transient
    private List<Object> reservations = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Veterinarian(String name, MedicalDepartment department, Hospital hospital, LocalTime workStartTime, LocalTime workEndTime) {
        validateFields(name, department, hospital, workStartTime, workEndTime);

        this.name = name;
        this.department = department;
        this.hospital = hospital;
        this.workStartTime = workStartTime;
        this.workEndTime = workEndTime;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(String name, MedicalDepartment department, Hospital hospital, LocalTime workStartTime, LocalTime workEndTime) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }

        if (department == null) {
            throw new IllegalArgumentException("진료 과목은 필수입니다.");
        }

        if (hospital == null) {
            throw new IllegalArgumentException("병원은 필수입니다.");
        }

        if (workStartTime == null) {
            throw new IllegalArgumentException("근무 시작 시간은 필수입니다.");
        }

        if (workEndTime == null) {
            throw new IllegalArgumentException("근무 종료 시간은 필수입니다.");
        }

        if (!workEndTime.isAfter(workStartTime)) {
            throw new IllegalArgumentException("근무 종료 시간은 시작 시간보다 늦어야 합니다.");
        }
    }

    // 비즈니스 메서드
    public boolean isWorkingTime(LocalTime time) {
        return !time.isBefore(workStartTime) && time.isBefore(workEndTime);
    }
}
