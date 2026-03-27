package PetMoa.PetMoa.domain.hospital.repository;

import PetMoa.PetMoa.domain.hospital.entity.QTimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.QVeterinarian;
import PetMoa.PetMoa.domain.hospital.entity.TimeSlot;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TimeSlotRepositoryImpl implements TimeSlotRepositoryCustom {

    private static final QTimeSlot timeSlot = QTimeSlot.timeSlot;
    private static final QVeterinarian veterinarian = QVeterinarian.veterinarian;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<TimeSlot> findByVeterinarianAndDate(Long veterinarianId, LocalDate date) {
        return queryFactory
                .selectFrom(timeSlot)
                .leftJoin(timeSlot.veterinarian, veterinarian).fetchJoin()
                .where(timeSlot.veterinarian.id.eq(veterinarianId)
                        .and(timeSlot.date.eq(date)))
                .orderBy(timeSlot.startTime.asc())
                .fetch();
    }

    @Override
    public List<TimeSlot> findAvailableSlots(Long veterinarianId, LocalDate date) {
        return queryFactory
                .selectFrom(timeSlot)
                .leftJoin(timeSlot.veterinarian, veterinarian).fetchJoin()
                .where(timeSlot.veterinarian.id.eq(veterinarianId)
                        .and(timeSlot.date.eq(date))
                        .and(timeSlot.currentReservations.lt(timeSlot.capacity)))
                .orderBy(timeSlot.startTime.asc())
                .fetch();
    }

    @Override
    public TimeSlot findByVeterinarianAndDateTime(Long veterinarianId, LocalDate date, LocalTime startTime) {
        return queryFactory
                .selectFrom(timeSlot)
                .leftJoin(timeSlot.veterinarian, veterinarian).fetchJoin()
                .where(timeSlot.veterinarian.id.eq(veterinarianId)
                        .and(timeSlot.date.eq(date))
                        .and(timeSlot.startTime.eq(startTime)))
                .fetchOne();
    }
}
