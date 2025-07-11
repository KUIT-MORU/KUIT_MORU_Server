package com.moru.backend.domain.log.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public record RoutineStepLogCreateRequest (
    @NotNull Integer stepOrder, // 몇 번째 스텝인지
    String note,
    @NotNull Duration actualTime,
    @NotNull LocalDateTime startAt,
    @NotNull LocalDateTime endedAt,
    @NotNull boolean isCompleted
) {}
