package com.moru.backend.domain.routine.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineTag;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "모든 루틴 목록 응답")
public class RoutineListResponse { 
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "루틴 제목", example = "아침 루틴")
    private String title;
    
    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
    
    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    private List<String> tags;
    
    @Schema(description = "루틴 좋아요 수", example = "16") // 좋아요 순으로 정렬할때 
    private Integer likeCount; // TODO: 실제 좋아요 수 반영 로직 구현 필요
    
    @Schema(description = "생성일시", example = "2024-01-01T09:00:00") // 최신순으로 정렬할때 
    private LocalDateTime createdAt;
    
    @Schema(description = "필요 시간(집중루틴만)", example = "00:50:00") // 시간순으로 정렬할 때 
    private String requiredTime;
    
    public static RoutineListResponse of(Routine routine, List<RoutineTag> tags) {
        return RoutineListResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .imageUrl(routine.getImageUrl())
                .tags(tags.stream()
                        .map(rt -> rt.getTag().getName())
                        .collect(Collectors.toList()))
                .likeCount(routine.getLikeCount()) // TODO: 실제 좋아요 수 반영 로직 구현 필요
                .createdAt(routine.getCreatedAt())
                .requiredTime(routine.getRequiredTime() != null ? routine.getRequiredTime().toString() : null)
                .build();
    }
} 