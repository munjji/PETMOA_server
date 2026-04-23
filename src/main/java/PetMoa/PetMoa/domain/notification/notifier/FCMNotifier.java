package PetMoa.PetMoa.domain.notification.notifier;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import PetMoa.PetMoa.domain.notification.dto.NotificationEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(2)
@ConditionalOnProperty(name = "notification.fcm.enabled", havingValue = "true")
public class FCMNotifier implements Notifier {

    @Override
    public void send(NotificationEvent event) {
        String title = buildTitle(event.eventType());
        String body = event.eventType().getDescription();

        // TODO: FCM 실제 구현 (Firebase Admin SDK)
        log.info("FCM 푸시 알림 전송 - userId: {}, title: {}, body: {}",
                event.userId(), title, body);
    }

    private String buildTitle(NotificationEventType eventType) {
        return switch (eventType) {
            case RESERVATION_CREATED, RESERVATION_CONFIRMED, RESERVATION_CANCELLED -> "예약 알림";
            case PAYMENT_COMPLETED, PAYMENT_FAILED, PAYMENT_REFUNDED -> "결제 알림";
            case TAXI_ASSIGNED -> "배차 알림";
        };
    }
}