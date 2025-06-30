package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.RoutineStep;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoutineStepResponse {
    private Integer stepOrder;
    private String name;
    private String estimatedTime;

    public static RoutineStepResponse from(RoutineStep step) {
        return RoutineStepResponse.builder()
                .stepOrder(step.getStepOrder())
                .name(step.getName())
                .estimatedTime(step.getEstimatedTime() != null ? step.getEstimatedTime().toString() : null)
                .build();
    }
}
