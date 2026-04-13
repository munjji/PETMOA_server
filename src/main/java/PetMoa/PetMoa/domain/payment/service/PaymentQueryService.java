package PetMoa.PetMoa.domain.payment.service;

import PetMoa.PetMoa.domain.payment.entity.Payment;
import PetMoa.PetMoa.domain.payment.repository.PaymentRepository;
import PetMoa.PetMoa.global.exception.ForbiddenException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentQueryService {

    private final PaymentRepository paymentRepository;

    public Payment getPaymentById(Long paymentId, Long userId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제를 찾을 수 없습니다. id=" + paymentId));
        validateOwnership(payment, userId);
        return payment;
    }

    public Payment getPaymentByOrderId(String orderId, Long userId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. orderId=" + orderId);
        }
        validateOwnership(payment, userId);
        return payment;
    }

    public Payment getPaymentByReservationId(Long reservationId, Long userId) {
        Payment payment = paymentRepository.findByReservationId(reservationId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. reservationId=" + reservationId);
        }
        validateOwnership(payment, userId);
        return payment;
    }

    public Payment getPaymentByPaymentKey(String paymentKey) {
        Payment payment = paymentRepository.findByPaymentKey(paymentKey);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. paymentKey=" + paymentKey);
        }
        return payment;
    }

    // 내부 서비스 호출용 메서드 (소유권 검증 없음)
    public Payment getPaymentByIdInternal(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("결제를 찾을 수 없습니다. id=" + paymentId));
    }

    public Payment getPaymentByOrderIdInternal(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. orderId=" + orderId);
        }
        return payment;
    }

    public Payment getPaymentByReservationIdInternal(Long reservationId) {
        Payment payment = paymentRepository.findByReservationId(reservationId);
        if (payment == null) {
            throw new EntityNotFoundException("결제를 찾을 수 없습니다. reservationId=" + reservationId);
        }
        return payment;
    }

    private void validateOwnership(Payment payment, Long userId) {
        Long ownerId = payment.getReservation().getUser().getId();
        if (!ownerId.equals(userId)) {
            throw new ForbiddenException("해당 결제의 소유자가 아닙니다.");
        }
    }
}
