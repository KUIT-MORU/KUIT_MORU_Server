package com.moru.backend.domain.routine.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class RoutineCreateRequest {
    @NotBlank
    @Size(max = 10)
    private String title;

    
}
