package com.moru.backend.domain.routine.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineApp;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.RoutineTag;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "집중 루틴 응답")
public class DetailedRoutineResponse {
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;
    
    @Schema(description = "루틴 제목", example = "아침 루틴")
    private String title;
    
    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;
    
    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    private List<String> tags;
    
    @Schema(description = "루틴 표시시 사용자 표시 여부", example = "true")
    private Boolean isUserVisible;
    
    @Schema(description = "루틴 설명", example = "매일 아침 건강한 하루를 시작하는 루틴입니다.")
    private String description;
    
    @Schema(description = "루틴 스텝 목록", example = "[\"물 마시기\", \"아침 운동\", \"아침 먹기\"]")
    private List<RoutineStepResponse> steps;
    
    @Schema(description = "연동된 앱 목록", example = "[\"앱1\", \"앱2\"]")
    private List<String> apps;
    
    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "수정일시", example = "2024-01-01T09:00:00")
    private LocalDateTime updatedAt;
    
    public static DetailedRoutineResponse of(Routine routine, List<RoutineTag> tags, List<RoutineStep> steps,
            List<RoutineApp> apps) {
        return DetailedRoutineResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .imageUrl(null) // 현재 Routine에 imageUrl 없음 → 나중에 확장
                .tags(tags.stream()
                        .map(rt -> rt.getTag().getName())
                        .collect(Collectors.toList()))
                .isUserVisible(routine.isUserVisible())
                .description(routine.getContent())
                .steps(steps.stream()
                        .map(RoutineStepResponse::from)
                        .collect(Collectors.toList()))
                .apps(apps.stream()
                        .map(ra -> ra.getApp().getName())
                        .collect(Collectors.toList()))
                .createdAt(routine.getCreatedAt())
                .updatedAt(routine.getUpdatedAt())
                .build();
    }
} 