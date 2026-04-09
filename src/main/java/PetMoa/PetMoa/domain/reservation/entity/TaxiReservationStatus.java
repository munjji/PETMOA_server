package PetMoa.PetMoa.domain.reservation.entity;

public enum TaxiReservationStatus {
    PENDING,      // 배차 대기
    ASSIGNED,     // 배차 완료
    PICKED_UP,    // 픽업 완료
    IN_PROGRESS,  // 이동 중
    COMPLETED,    // 운행 완료
    CANCELLED     // 취소됨
}
