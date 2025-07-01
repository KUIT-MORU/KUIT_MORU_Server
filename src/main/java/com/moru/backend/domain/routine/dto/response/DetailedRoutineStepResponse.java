package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.RoutineStep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "루틴 스텝 응답")
public class DetailedRoutineStepResponse {
    @Schema(description = "스텝 순서", example = "1")
    private Integer stepOrder;

    @Schema(description = "스텝 이름", example = "물 마시기")
    private String name;

    @Schema(description = "소요시간", example = "00:05:00")
    private String estimatedTime;

    public static DetailedRoutineStepResponse from(RoutineStep step) {
        return DetailedRoutineStepResponse.builder()
                .stepOrder(step.getStepOrder())
                .name(step.getName())
                .estimatedTime(step.getEstimatedTime() != null ? step.getEstimatedTime().toString() : null)
                .build();
    }
}
