package PetMoa.PetMoa.domain.notification.consumer;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import PetMoa.PetMoa.domain.notification.notifier.Notifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventConsumer {

    private final List<Notifier> notifiers;

    @KafkaListener(
            topics = "${notification.kafka.topic}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(NotificationEvent event) {
        log.info("알림 이벤트 수신 - eventId: {}, type: {}", event.eventId(), event.eventType());

        for (Notifier notifier : notifiers) {
            try {
                notifier.send(event);
            } catch (Exception e) {
                log.error("알림 전송 실패 - notifier: {}, eventId: {}",
                        notifier.getClass().getSimpleName(), event.eventId(), e);
            }
        }
    }
}