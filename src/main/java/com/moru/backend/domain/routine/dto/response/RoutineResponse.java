package com.moru.backend.domain.routine.dto.response;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineApp;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.RoutineTag;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoutineResponse {
    private UUID id;
    private String title;
    private String imageUrl;
    private List<String> tags; // todo : Tags list로 변경 
    private Boolean isUserVisible;
    private String description; // nullable 가능 
    private List<RoutineStepResponse> steps;
    private List<String> apps; // todo : App list로 변경 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RoutineResponse of(Routine routine, List<RoutineTag> tags, List<RoutineStep> steps,
            List<RoutineApp> apps) {
                return RoutineResponse.builder()
                .id(routine.getId())
                .title(routine.getTitle())
                .imageUrl(null) // 현재 Routine에 imageUrl 없음 → 나중에 확장
                .tags(tags.stream()
                        .map(rt -> rt.getTag().getName())
                        .collect(Collectors.toList()))
                .isUserVisible(routine.isUserVisible()) // ← 실제는 isUserVisible 필드에 해당
                .description(routine.getContent()) // DB에선 content, API에선 description
                .steps(steps.stream()
                        .map(RoutineStepResponse::from)
                        .collect(Collectors.toList()))
                .apps(apps.stream()
                        .map(ra -> ra.getApp().getName()) // 또는 getApp().getId().toString()
                        .collect(Collectors.toList()))
                .createdAt(routine.getCreatedAt())
                .updatedAt(routine.getUpdatedAt())
                .build();
    }
}
