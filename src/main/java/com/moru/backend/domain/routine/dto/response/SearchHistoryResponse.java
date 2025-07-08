package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "검색 기록 응답")
public class SearchHistoryResponse {
    @Schema(description = "검색 기록 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "검색 키워드", example = "아침")
    private String searchKeyword;

    @Schema(description = "검색 타입", example = "ROUTINE_NAME")
    private String searchType;

    @Schema(description = "검색일시", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;
}
