package com.moru.backend.domain.log.dto;

import java.util.List;

public record RoutineLogListResponse(
        List<RoutineLogSummaryResponse> logs
) {
}
