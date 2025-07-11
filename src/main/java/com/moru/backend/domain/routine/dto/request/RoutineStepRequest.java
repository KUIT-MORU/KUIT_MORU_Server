package com.moru.backend.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.Duration;

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

    @Schema(description = "소요시간(집중 루틴만 값, 간편 루틴은 null)", example = "PT5M")
    Duration estimatedTime // 집중 루틴만 값, 간편 루틴은 null (ISO-8601 Duration 형식: PT5M = 5분, PT2H30M = 2시간 30분)
) {}
