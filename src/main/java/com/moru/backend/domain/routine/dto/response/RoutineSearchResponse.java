package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "루틴 검색 결과")
public class RoutineSearchResponse {
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "루틴 제목", example = "아침 루틴")
    private String title;

    @Schema(description = "루틴 이미지 URl", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\"]")
    private List<String> tags;

    @Schema(description = "좋아요 수", example = "16")
    private Integer likeCount;

    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;

}
