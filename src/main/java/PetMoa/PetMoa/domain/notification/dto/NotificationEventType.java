package PetMoa.PetMoa.domain.notification.dto;

public enum NotificationEventType {
    // 예약 관련
    RESERVATION_CREATED("예약이 생성되었습니다."),
    RESERVATION_CONFIRMED("예약이 확정되었습니다."),
    RESERVATION_CANCELLED("예약이 취소되었습니다."),

    // 결제 관련
    PAYMENT_COMPLETED("결제가 완료되었습니다."),
    PAYMENT_FAILED("결제가 실패했습니다."),
    PAYMENT_REFUNDED("환불이 완료되었습니다."),

    // 택시 배차 관련
    TAXI_ASSIGNED("택시 배차가 완료되었습니다.");

    private final String description;

    NotificationEventType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
