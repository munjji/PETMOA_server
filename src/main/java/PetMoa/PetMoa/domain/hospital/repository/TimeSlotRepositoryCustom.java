package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface TimeSlotRepositoryCustom {

    /**
     * 특정 수의사의 특정 날짜 타임슬롯 조회
     */
    List<TimeSlot> findByVeterinarianAndDate(Long veterinarianId, LocalDate date);

    /**
     * 예약 가능한 타임슬롯 조회 (정원 미달)
     */
    List<TimeSlot> findAvailableSlots(Long veterinarianId, LocalDate date);

    /**
     * 특정 수의사의 특정 시간 타임슬롯 조회
     */
    TimeSlot findByVeterinarianAndDateTime(Long veterinarianId, LocalDate date, LocalTime startTime);
}
