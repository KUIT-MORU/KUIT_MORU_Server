package com.moru.backend.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
@Schema(description = "루틴 스텝 요청")
public class RoutineStepRequest {
    @Schema(description = "스텝 이름", example = "물 마시기")
    @NotBlank
    @Size(max = 22)
    private String name;

    @Schema(description = "스텝 순서", example = "1")
    @NotNull
    private Integer stepOrder;

    @Schema(description = "소요시간", example = "00:05:00")
    private String estimatedTime; // 소요시간 ; 선택 (HH:MM:SS 형식)
}
