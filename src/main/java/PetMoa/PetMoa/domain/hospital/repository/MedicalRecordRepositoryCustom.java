package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.MedicalRecord;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalRecordRepositoryCustom {

    /**
     * 반려동물별 진료 기록 조회 (최신순)
     */
    List<MedicalRecord> findByPetIdOrderByVisitDateDesc(Long petId);

    /**
     * 특정 기간 진료 기록 조회
     */
    List<MedicalRecord> findByPetIdAndDateRange(Long petId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 병원 예약별 진료 기록 조회
     */
    MedicalRecord findByHospitalReservationId(Long hospitalReservationId);
}
