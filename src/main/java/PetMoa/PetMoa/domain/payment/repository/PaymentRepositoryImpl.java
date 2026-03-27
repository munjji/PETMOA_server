package PetMoa.PetMoa.domain.payment.repository;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.entity.QPayment;
import PetMoa.PetMoa.domain.reservation.entity.QReservation;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Payment findByReservationId(Long reservationId) {
        QPayment payment = QPayment.payment;
        QReservation reservation = QReservation.reservation;

        return queryFactory
                .selectFrom(payment)
                .leftJoin(payment.reservation, reservation).fetchJoin()
                .where(payment.reservation.id.eq(reservationId))
                .fetchOne();
    }

    @Override
    public Payment findByOrderId(String orderId) {
        QPayment payment = QPayment.payment;
        QReservation reservation = QReservation.reservation;

        return queryFactory
                .selectFrom(payment)
                .leftJoin(payment.reservation, reservation).fetchJoin()
                .where(payment.orderId.eq(orderId))
                .fetchOne();
    }

    @Override
    public Payment findByPaymentKey(String paymentKey) {
        QPayment payment = QPayment.payment;
        QReservation reservation = QReservation.reservation;

        return queryFactory
                .selectFrom(payment)
                .leftJoin(payment.reservation, reservation).fetchJoin()
                .where(payment.paymentKey.eq(paymentKey))
                .fetchOne();
    }
}
