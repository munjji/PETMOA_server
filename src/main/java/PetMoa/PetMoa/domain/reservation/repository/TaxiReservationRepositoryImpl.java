package PetMoa.PetMoa.domain.reservation.repository;

import PetMoa.PetMoa.domain.reservation.entity.QTaxiReservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class TaxiReservationRepositoryImpl implements TaxiReservationRepositoryCustom {

    private static final QTaxiReservation taxiReservation = QTaxiReservation.taxiReservation;

    private final JPAQueryFactory queryFactory;

    @Override
    public Set<Long> findReservedTaxiIdsBetween(LocalDateTime startTime, LocalDateTime endTime) {
        return new HashSet<>(queryFactory
                .select(taxiReservation.taxi.id)
                .from(taxiReservation)
                .where(taxiReservation.pickupTime.between(startTime, endTime))
                .fetch());
    }
}
