package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.RoutineStep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "루틴 스텝 상세 응답")
public record RoutineStepDetailResponse(
    @Schema(description = "스텝 ID", example = "123e4567-e89b-12d3-a456-426614174000")
    UUID id,

    @Schema(description = "스텝 순서", example = "1")
    Integer stepOrder,

    @Schema(description = "스텝 이름", example = "물 마시기")
    String name,
    
    @Schema(description = "소요시간(집중 루틴만, 간편 루틴은 null)", example = "00:05:00")
    String estimatedTime
) {
    public static RoutineStepDetailResponse from(RoutineStep step) {
        return new RoutineStepDetailResponse(
                step.getId(),
                step.getStepOrder(),
                step.getName(),
                step.getEstimatedTime() != null ? step.getEstimatedTime().toString() : null
        );
    }
}
