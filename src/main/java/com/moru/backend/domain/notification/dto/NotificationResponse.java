package com.moru.backend.domain.notification.dto;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID senderId,
        String senderNickname,
        String senderProfileImage,
        String message,
        String relativeTime
) {}
