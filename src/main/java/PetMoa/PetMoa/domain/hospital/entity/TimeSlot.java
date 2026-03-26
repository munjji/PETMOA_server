package PetMoa.PetMoa.domain.hospital.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "time_slots",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"veterinarian_id", "date", "start_time"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinarian_id", nullable = false)
    private Veterinarian veterinarian;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Integer currentReservations;

    @Column(nullable = false)
    private Boolean isAvailable;

    @Version
    private Long version;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public TimeSlot(Veterinarian veterinarian, LocalDate date, LocalTime startTime, LocalTime endTime, Integer capacity) {
        validateFields(veterinarian, date, startTime, endTime, capacity);

        this.veterinarian = veterinarian;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.capacity = (capacity != null) ? capacity : 1;
        this.currentReservations = 0;
        this.isAvailable = true;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(Veterinarian veterinarian, LocalDate date, LocalTime startTime, LocalTime endTime, Integer capacity) {
        if (veterinarian == null) {
            throw new IllegalArgumentException("수의사는 필수입니다.");
        }

        if (date == null) {
            throw new IllegalArgumentException("날짜는 필수입니다.");
        }

        if (startTime == null) {
            throw new IllegalArgumentException("시작 시간은 필수입니다.");
        }

        if (endTime == null) {
            throw new IllegalArgumentException("종료 시간은 필수입니다.");
        }

        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("종료 시간은 시작 시간보다 늦어야 합니다.");
        }

        if (capacity != null && capacity < 1) {
            throw new IllegalArgumentException("정원은 1 이상이어야 합니다.");
        }
    }

    // 비즈니스 메서드
    public boolean hasAvailableSpace() {
        return currentReservations < capacity;
    }

    public void incrementReservations() {
        if (!hasAvailableSpace()) {
            throw new IllegalStateException("예약이 마감되었습니다.");
        }
        this.currentReservations++;
        updateAvailability();
    }

    public void decrementReservations() {
        if (currentReservations <= 0) {
            throw new IllegalStateException("예약 수는 0보다 작을 수 없습니다.");
        }
        this.currentReservations--;
        updateAvailability();
    }

    private void updateAvailability() {
        this.isAvailable = hasAvailableSpace();
    }
}
