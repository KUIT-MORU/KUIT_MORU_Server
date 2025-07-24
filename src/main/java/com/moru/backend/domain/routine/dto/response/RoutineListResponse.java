package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import lombok.Builder;

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
    Integer likeCount, 
    
    @Schema(description = "생성일시", example = "2024-01-01T09:00:00") // 최신순으로 정렬할때 
    LocalDateTime createdAt,
    
    @Schema(description = "필요 시간(집중루틴만)", example = "PT50M") // 시간순으로 정렬할 때 
    Duration requiredTime
) {
    /**
     * 일반 Routine 엔티티 기반 카드 응답 생성 (공개/소유 루틴 등)
     * Routine + List<RoutineTag> → RoutineListResponse
     */
    public static RoutineListResponse fromRoutine(Routine routine, String imageFullUrl, List<RoutineTag> tags) {
        return RoutineListResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .imageUrl(imageFullUrl)
                .tags(tags.stream().map(rt -> rt.getTag().getName()).toList())
                .likeCount(routine.getLikeCount())
                .createdAt(routine.getCreatedAt())
                .requiredTime(routine.getRequiredTime())
                .build();
    }

    /**
     * 실행중인 루틴(스냅샷 기반) 카드 응답 생성
     * RoutineSnapshot → RoutineListResponse
     */
    public static RoutineListResponse fromSnapshot(RoutineSnapshot snapshot, String imageFullUrl) {
        return RoutineListResponse.builder()
                .id(snapshot.getOriginalRoutineId())
                .title(snapshot.getTitle())
                .imageUrl(imageFullUrl)
                .tags(snapshot.getTagSnapshots().stream().map(RoutineTagSnapshot::getTagName).toList())
                .likeCount(0)
                .build();
    }
} 