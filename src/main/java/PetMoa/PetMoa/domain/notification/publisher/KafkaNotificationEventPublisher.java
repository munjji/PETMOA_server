package PetMoa.PetMoa.domain.notification.publisher;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class KafkaNotificationEventPublisher implements NotificationEventPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    @Value("${notification.kafka.topic}")
    private String topicName;

    @Override
    public void publish(NotificationEvent event) {
        log.info("알림 이벤트 발행 - eventId: {}, type: {}", event.eventId(), event.eventType());

        kafkaTemplate.send(topicName, event.userId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("알림 이벤트 발행 실패 - eventId: {}", event.eventId(), ex);
                    } else {
                        log.debug("알림 이벤트 발행 성공 - eventId: {}, partition: {}, offset: {}",
                                event.eventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}