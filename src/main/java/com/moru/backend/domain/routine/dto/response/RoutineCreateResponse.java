package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "루틴 생성 응답")
public class RoutineCreateResponse {
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "루틴 제목", example = "아침 루틴")
    private String title;

    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;
} 