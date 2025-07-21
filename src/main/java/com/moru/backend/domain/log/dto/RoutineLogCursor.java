package com.moru.backend.domain.log.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoutineLogCursor(
        LocalDateTime createdAt,
        UUID logId
) {}
