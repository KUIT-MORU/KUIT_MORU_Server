package com.moru.backend.domain.log.dto;

import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public record RoutineLogSummaryResponse (
    UUID logId,
    String routineTitle,
    boolean isCompleted,
    Duration totalTime,
    String imageUrl,
    List<String> tags //snapshot에서 꺼낸 태그
) {
    public static RoutineLogSummaryResponse from (
            RoutineSnapshot snapshot,
            RoutineLog log,
            List<String> tags
    ) {
        return new RoutineLogSummaryResponse(
                log.getId(),
                snapshot.getTitle(),
                log.isCompleted(),
                log.getTotalTime(),
                snapshot.getImageUrl(),
                tags
        );
    }
}
