package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.RoutineStep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "간편 루틴 스텝 생성용 응답")
public class SimpleRoutineStepResponse {
    @Schema(description = "스텝 순서", example = "1")
    private Integer stepOrder;
    
    @Schema(description = "스텝 이름", example = "물 마시기")
    private String name;

    public static SimpleRoutineStepResponse from(RoutineStep step) {
        return SimpleRoutineStepResponse.builder()
                .stepOrder(step.getStepOrder())
                .name(step.getName())
                .build();
    }
} 