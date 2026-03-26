package PetMoa.PetMoa.domain.taxi.entity;

/**
 * 택시 상태
 */
public enum TaxiStatus {
    AVAILABLE,      // 예약 가능
    IN_SERVICE,     // 운행 중
    MAINTENANCE,    // 정비 중
    UNAVAILABLE     // 이용 불가
}
