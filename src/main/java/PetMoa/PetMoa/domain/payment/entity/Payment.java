package PetMoa.PetMoa.domain.payment.entity;

import PetMoa.PetMoa.domain.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(nullable = false, unique = true, length = 100)
    private String orderId;

    @Column(length = 100)
    private String paymentKey;

    @Column(nullable = false)
    private Integer totalAmount;

    @Column(nullable = false)
    private Integer depositAmount;

    @Column(nullable = false)
    private Integer taxiFare;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    private LocalDateTime paidAt;

    private LocalDateTime cancelledAt;

    private Integer refundAmount;

    @Column(length = 500)
    private String cancelReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Payment(Reservation reservation, String orderId, Integer depositAmount, Integer taxiFare, PaymentMethod method) {
        validateFields(reservation, orderId, depositAmount, taxiFare, method);

        this.reservation = reservation;
        this.orderId = orderId;
        this.depositAmount = depositAmount;
        this.taxiFare = taxiFare;
        this.totalAmount = depositAmount + taxiFare;
        this.method = method;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    private void validateFields(Reservation reservation, String orderId, Integer depositAmount, Integer taxiFare, PaymentMethod method) {
        if (reservation == null) {
            throw new IllegalArgumentException("예약은 필수입니다.");
        }

        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 ID는 필수입니다.");
        }

        if (depositAmount == null) {
            throw new IllegalArgumentException("예약금은 필수입니다.");
        }

        if (taxiFare == null) {
            throw new IllegalArgumentException("택시비는 필수입니다.");
        }

        if (method == null) {
            throw new IllegalArgumentException("결제 수단은 필수입니다.");
        }
    }

    // 비즈니스 메서드
    public void approve(String paymentKey) {
        if (this.status != PaymentStatus.PENDING) {
            throw new IllegalStateException("대기 중인 결제만 승인할 수 있습니다.");
        }
        this.paymentKey = paymentKey;
        this.status = PaymentStatus.APPROVED;
        this.paidAt = LocalDateTime.now();
    }

    public void cancel(String cancelReason) {
        if (!canRefund()) {
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.refundAmount = this.totalAmount;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void partialCancel(Integer partialRefundAmount, String cancelReason) {
        if (!canRefund()) {
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }
        if (partialRefundAmount == null || partialRefundAmount <= 0 || partialRefundAmount > this.totalAmount) {
            throw new IllegalArgumentException("부분 환불 금액이 올바르지 않습니다.");
        }
        this.status = PaymentStatus.PARTIAL_CANCELLED;
        this.refundAmount = partialRefundAmount;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * 당일 취소로 인한 환불 불가 처리
     */
    public void cancelWithNoRefund(String cancelReason) {
        if (!canRefund()) {
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }
        this.status = PaymentStatus.CANCELLED;
        this.refundAmount = 0;
        this.cancelReason = cancelReason;
        this.cancelledAt = LocalDateTime.now();
    }

    public void fail() {
        this.status = PaymentStatus.FAILED;
    }

    public boolean isApproved() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean canRefund() {
        return this.status == PaymentStatus.APPROVED;
    }
}
