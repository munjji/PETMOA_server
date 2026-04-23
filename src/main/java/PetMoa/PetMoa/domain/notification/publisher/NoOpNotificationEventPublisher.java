package PetMoa.PetMoa.domain.notification.publisher;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnMissingBean(NotificationEventPublisher.class)
public class NoOpNotificationEventPublisher implements NotificationEventPublisher {

    @Override
    public void publish(NotificationEvent event) {
        log.debug("알림 이벤트 발행 (NoOp) - Kafka 미설정. eventId: {}, type: {}",
                event.eventId(), event.eventType());
    }
}