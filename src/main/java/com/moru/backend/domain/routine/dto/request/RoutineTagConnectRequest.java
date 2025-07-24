package com.moru.backend.domain.routine.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record RoutineTagConnectRequest(
        @NotEmpty(message = "태그 ID 리스트는 필수입니다.")
        List<@NotNull UUID> tagIds
) {
}
