package com.moru.backend.domain.routine.domain.dto.response;

import java.util.List;
import java.util.UUID;
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
    private Boolean isPublic;
    private String description; // nullable 가능 
    private List<RoutineStepResponse> steps;
    private List<String> apps; // todo : App list로 변경 
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
