package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.hospital.entity.QTimeSlot;
import PetMoa.PetMoa.domain.hospital.entity.QVeterinarian;
import PetMoa.PetMoa.domain.hospital.entity.QHospital;
import PetMoa.PetMoa.domain.pet.entity.QPet;
import PetMoa.PetMoa.domain.reservation.entity.QHospitalReservation;
import PetMoa.PetMoa.domain.reservation.entity.QReservation;
import PetMoa.PetMoa.domain.reservation.entity.QTaxiReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.taxi.entity.QPetTaxi;
import PetMoa.PetMoa.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private static final QReservation reservation = QReservation.reservation;
    private static final QUser user = QUser.user;
    private static final QPet pet = QPet.pet;
    private static final QHospitalReservation hospitalReservation = QHospitalReservation.hospitalReservation;
    private static final QVeterinarian veterinarian = QVeterinarian.veterinarian;
    private static final QTimeSlot timeSlot = QTimeSlot.timeSlot;
    private static final QHospital hospital = QHospital.hospital;
    private static final QTaxiReservation taxiReservation = QTaxiReservation.taxiReservation;
    private static final QPetTaxi petTaxi = QPetTaxi.petTaxi;

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Reservation> findByUserIdWithDetails(Long userId) {
        return queryFactory
                .selectFrom(reservation)
                .distinct()
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.pet, pet).fetchJoin()
                .leftJoin(reservation.hospitalReservation, hospitalReservation).fetchJoin()
                .leftJoin(hospitalReservation.veterinarian, veterinarian).fetchJoin()
                .leftJoin(veterinarian.hospital, hospital).fetchJoin()
                .leftJoin(hospitalReservation.timeSlot, timeSlot).fetchJoin()
                .leftJoin(reservation.taxiReservations, taxiReservation).fetchJoin()
                .leftJoin(taxiReservation.taxi, petTaxi).fetchJoin()
                .where(reservation.user.id.eq(userId))
                .orderBy(reservation.createdAt.desc())
                .fetch();
    }

    @Override
    public Reservation findByIdWithDetails(Long reservationId) {
        return queryFactory
                .selectFrom(reservation)
                .distinct()
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.pet, pet).fetchJoin()
                .leftJoin(reservation.hospitalReservation, hospitalReservation).fetchJoin()
                .leftJoin(hospitalReservation.veterinarian, veterinarian).fetchJoin()
                .leftJoin(veterinarian.hospital, hospital).fetchJoin()
                .leftJoin(hospitalReservation.timeSlot, timeSlot).fetchJoin()
                .leftJoin(reservation.taxiReservations, taxiReservation).fetchJoin()
                .leftJoin(taxiReservation.taxi, petTaxi).fetchJoin()
                .where(reservation.id.eq(reservationId))
                .fetchOne();
    }
}
