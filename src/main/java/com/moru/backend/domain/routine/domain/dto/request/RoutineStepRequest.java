package com.moru.backend.domain.routine.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RoutineStepRequest {
    @NotBlank
    @Size(max = 22)
    private String name;

    @NotNull
    private Integer stepOrder;

    private String estimatedTime; // 소요시간 ; 선택 (HH:MM:SS 형식)
}
