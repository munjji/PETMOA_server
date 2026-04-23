package PetMoa.PetMoa.domain.notification.publisher;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;

public interface NotificationEventPublisher {
    void publish(NotificationEvent event);
}