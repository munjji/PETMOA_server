package PetMoa.PetMoa.domain.notification.notifier;

import PetMoa.PetMoa.domain.notification.dto.NotificationEvent;

public interface Notifier {
    void send(NotificationEvent event);
}