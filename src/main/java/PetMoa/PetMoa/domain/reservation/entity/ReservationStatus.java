package PetMoa.PetMoa.domain.reservation.entity;

/**
 * 예약 상태
 */
public enum ReservationStatus {
    PENDING,        // 대기 (결제 전)
    CONFIRMED,      // 확정 (결제 완료)
    CANCELLED,      // 취소
    COMPLETED,      // 완료
    NO_SHOW         // 노쇼
}
