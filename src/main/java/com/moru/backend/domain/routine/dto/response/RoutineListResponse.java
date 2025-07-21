package com.moru.backend.domain.routine.dto.response;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "루틴 목록 응답")
public record RoutineListResponse(
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,
    
    @Schema(description = "루틴 제목", example = "아침 루틴")
    String title,
    
    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,
    
    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    List<String> tags,
    
    @Schema(description = "루틴 좋아요 수", example = "16") // 좋아요 순으로 정렬할때 
    Integer likeCount, // TODO: 실제 좋아요 수 반영 로직 구현 필요
    
    @Schema(description = "생성일시", example = "2024-01-01T09:00:00") // 최신순으로 정렬할때 
    LocalDateTime createdAt,
    
    @Schema(description = "필요 시간(집중루틴만)", example = "PT50M") // 시간순으로 정렬할 때 
    Duration requiredTime
) {
    public static RoutineListResponse fromRoutine(Routine routine, List<RoutineTag> tags) {
        return new RoutineListResponse(
                routine.getId(),
                routine.getTitle(),
                routine.getImageUrl(),
                tags.stream()
                        .map(rt -> rt.getTag().getName())
                        .collect(Collectors.toList()),
                routine.getLikeCount(),
                routine.getCreatedAt(),
                routine.getRequiredTime()
        );
    }
} 