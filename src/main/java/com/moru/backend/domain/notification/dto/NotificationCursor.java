package com.moru.backend.domain.notification.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationCursor(
        LocalDateTime createdAt,
        UUID id
) {}
