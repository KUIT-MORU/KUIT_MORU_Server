package com.moru.backend.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@Schema(description = "루틴 스텝 요청")
public record RoutineStepRequest(
    @Schema(description = "스텝 이름", example = "물 마시기")
    @NotBlank
    @Size(max = 22)
    String name,

    @Schema(description = "스텝 순서", example = "1")
    @NotNull
    Integer stepOrder,

    @Schema(description = "소요시간(집중 루틴만 값, 간편 루틴은 null)", example = "00:05:00")
    String estimatedTime // 집중 루틴만 값, 간편 루틴은 null (HH:MM:SS 형식)
) {}
