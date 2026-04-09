package PetMoa.PetMoa.domain.payment.service;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제를 찾을 수 없습니다. id=" + paymentId));
    }

    public Payment getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. orderId=" + orderId);
        }
        return payment;
    }

    public Payment getPaymentByReservationId(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. reservationId=" + reservationId);
        }
        return payment;
    }

    public Payment getPaymentByPaymentKey(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. paymentKey=" + paymentKey);
        }
        return payment;
    }
}
