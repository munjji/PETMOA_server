package PetMoa.PetMoa.domain.notification.notifier;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
public class LogNotifier implements Notifier {

    @Override
    public void send(NotificationEvent event) {
        log.info("""
                ========== 알림 발송 ==========
                이벤트 ID: {}
                이벤트 타입: {}
                수신자: {} (ID: {})
                연락처: {}, {}
                메시지: {}
                상세 정보: {}
                발생 시간: {}
                ===============================
                """,
                event.eventId(),
                event.eventType(),
                event.userName(),
                event.userId(),
                event.userPhoneNumber(),
                event.userEmail(),
                event.eventType().getDescription(),
                event.payload(),
                event.occurredAt()
        );
    }
}
