package com.moru.backend.domain.log.dto;

import java.time.Duration;

public record RoutineStepLogDto(
        int stepOrder,
        String stepName,
        String note,
        Duration estimatedTime,
        Duration actualTime
) {}
