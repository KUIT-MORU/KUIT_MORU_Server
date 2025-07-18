package com.moru.backend.domain.routine.dto.response;

import java.util.List;

public record RecommendFeedResponse(
    List<RoutineListResponse> hotRoutines,
    List<RoutineListResponse> personalRoutines,
    TagPairSection tagPairSection1,
    TagPairSection tagPairSection2
) {}