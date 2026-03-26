package PetMoa.PetMoa.domain.payment.entity;

/**
 * 결제 상태
 */
public enum PaymentStatus {
    PENDING,            // 결제 대기
    APPROVED,           // 승인 완료
    CANCELLED,          // 취소
    PARTIAL_CANCELLED,  // 부분 취소
    FAILED              // 실패
}
