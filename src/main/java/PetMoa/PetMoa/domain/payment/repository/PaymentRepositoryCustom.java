package PetMoa.PetMoa.domain.payment.repository;

import PetMoa.PetMoa.domain.payment.entity.Payment;

public interface PaymentRepositoryCustom {

    /**
     * 예약으로 결제 조회
     */
    Payment findByReservationId(Long reservationId);

    /**
     * 주문 ID로 결제 조회
     */
    Payment findByOrderId(String orderId);

    /**
     * 결제키로 결제 조회
     */
    Payment findByPaymentKey(String paymentKey);
}
