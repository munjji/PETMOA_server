package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.pet.entity.QPet;
import PetMoa.PetMoa.domain.reservation.entity.QReservation;
import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import PetMoa.PetMoa.domain.user.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Reservation> findByUserIdWithDetails(Long userId) {
        QReservation reservation = QReservation.reservation;
        QUser user = QUser.user;
        QPet pet = QPet.pet;

        return queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.pet, pet).fetchJoin()
                .where(reservation.user.id.eq(userId))
                .orderBy(reservation.createdAt.desc())
                .fetch();
    }

    @Override
    public Reservation findByIdWithDetails(Long reservationId) {
        QReservation reservation = QReservation.reservation;
        QUser user = QUser.user;
        QPet pet = QPet.pet;

        return queryFactory
                .selectFrom(reservation)
                .leftJoin(reservation.user, user).fetchJoin()
                .leftJoin(reservation.pet, pet).fetchJoin()
                .where(reservation.id.eq(reservationId))
                .fetchOne();
    }
}
