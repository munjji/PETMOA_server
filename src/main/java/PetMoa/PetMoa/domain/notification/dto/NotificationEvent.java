package PetMoa.PetMoa.domain.notification.dto;

import PetMoa.PetMoa.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record NotificationEvent(
        String eventId,
        NotificationEventType eventType,
        Long userId,
        String userName,
        String userPhoneNumber,
        String userEmail,
        Map<String, Object> payload,
        LocalDateTime occurredAt
) {
    public static NotificationEvent of(
            NotificationEventType eventType,
            User user,
            Map<String, Object> payload) {
        return new NotificationEvent(
                UUID.randomUUID().toString(),
                eventType,
                user.getId(),
                user.getName(),
                user.getPhoneNumber(),
                user.getEmail(),
                payload,
                LocalDateTime.now()
        );
    }
}