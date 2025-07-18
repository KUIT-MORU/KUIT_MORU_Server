package com.moru.backend.domain.routine.dto.response;

import java.util.List;

public record TagPairSection(
    String tag1,
    String tag2,
    List<RoutineListResponse> routines
) {} 