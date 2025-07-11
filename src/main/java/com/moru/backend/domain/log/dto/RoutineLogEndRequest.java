package com.moru.backend.domain.log.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record RoutineLogEndRequest(
        LocalDateTime endedAt,
        Duration totalTime
) {
}
