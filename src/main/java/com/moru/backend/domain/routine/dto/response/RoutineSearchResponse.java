package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Schema(description = "루틴 검색 결과")
public record RoutineSearchResponse(
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "루틴 제목", example = "아침 루틴")
    String title,

    @Schema(description = "루틴 이미지 URl", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\"]")
    List<String> tags,

    @Schema(description = "좋아요 수", example = "16")
    Integer likeCount,

    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    LocalDateTime createdAt,

    @Schema(description = "해당 루틴의 소유주가 루틴을 실행 중인지 여부", example = "false")
    boolean isRunning

) {

    public static RoutineSearchResponse of(RoutineListResponse routineListResponse, boolean isRunning) {
        return new RoutineSearchResponse(
            routineListResponse.id(),
            routineListResponse.title(),
            routineListResponse.imageUrl(),
            routineListResponse.tags(),
            routineListResponse.likeCount(),
            routineListResponse.createdAt(),
            isRunning
        );
    }
}
