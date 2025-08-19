package com.moru.backend.domain.log.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "루틴 스텝 로그 생성")
public record RoutineStepLogCreateRequest (
    @Schema(description = "몇 번째 스텝인지", example = "1")
    @NotNull Integer stepOrder, // 몇 번째 스텝인지

    @Schema(description = "스텝에 대한 메모", example = "물 1잔 마시기 완료")
    String note,

    @Schema(description = "실제 소요 시간", example = "PT5M") // ISO-8601 Duration 표현
    @NotNull Duration actualTime,

    @Schema(description = "시작 시각", example = "2025-08-17T09:00:00")
    @NotNull LocalDateTime startAt,

    @Schema(description = "종료 시각", example = "2025-08-17T09:05:00")
    @NotNull LocalDateTime endedAt,

    @Schema(description = "스텝 완료 여부", example = "true")
    @NotNull boolean isCompleted
) {}
