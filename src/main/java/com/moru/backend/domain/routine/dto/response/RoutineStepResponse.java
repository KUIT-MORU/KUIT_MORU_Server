package com.moru.backend.domain.routine.domain.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoutineStepResponse {
    private Integer stepOrder;
    private String name;
    private String estimatedTime;
}
