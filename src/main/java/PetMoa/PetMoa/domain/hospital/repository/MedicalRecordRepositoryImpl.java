package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.MedicalRecord;
import PetMoa.PetMoa.domain.hospital.entity.QMedicalRecord;
import PetMoa.PetMoa.domain.pet.entity.QPet;
import PetMoa.PetMoa.domain.reservation.entity.QHospitalReservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MedicalRecordRepositoryImpl implements MedicalRecordRepositoryCustom {

    private static final QMedicalRecord medicalRecord = QMedicalRecord.medicalRecord;
    private static final QPet pet = QPet.pet;
    private static final QHospitalReservation hospitalReservation = QHospitalReservation.hospitalReservation;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MedicalRecord> findByPetIdOrderByVisitDateDesc(Long petId) {
        return queryFactory
                .selectFrom(medicalRecord)
                .leftJoin(medicalRecord.pet, pet).fetchJoin()
                .leftJoin(medicalRecord.hospitalReservation, hospitalReservation).fetchJoin()
                .where(medicalRecord.pet.id.eq(petId))
                .orderBy(medicalRecord.visitDate.desc())
                .fetch();
    }

    @Override
    public List<MedicalRecord> findByPetIdAndDateRange(Long petId, LocalDateTime startDate, LocalDateTime endDate) {
        return queryFactory
                .selectFrom(medicalRecord)
                .leftJoin(medicalRecord.pet, pet).fetchJoin()
                .leftJoin(medicalRecord.hospitalReservation, hospitalReservation).fetchJoin()
                .where(medicalRecord.pet.id.eq(petId)
                        .and(medicalRecord.visitDate.between(startDate, endDate)))
                .orderBy(medicalRecord.visitDate.desc())
                .fetch();
    }

    @Override
    public MedicalRecord findByHospitalReservationId(Long hospitalReservationId) {
        return queryFactory
                .selectFrom(medicalRecord)
                .leftJoin(medicalRecord.pet, pet).fetchJoin()
                .leftJoin(medicalRecord.hospitalReservation, hospitalReservation).fetchJoin()
                .where(medicalRecord.hospitalReservation.id.eq(hospitalReservationId))
                .fetchOne();
    }
}
