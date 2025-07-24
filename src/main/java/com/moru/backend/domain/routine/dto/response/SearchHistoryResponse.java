package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import java.util.UUID;

import java.time.LocalDateTime;

@Builder
@Schema(description = "검색 기록 응답")
public record SearchHistoryResponse(
    @Schema(description = "검색 기록 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "검색 키워드", example = "아침")
    String searchKeyword,

    @Schema(description = "검색 타입", example = "ROUTINE_NAME")
    String searchType,

    @Schema(description = "검색일시", example = "2024-01-01T09:00:00")
    LocalDateTime createdAt
) {}
