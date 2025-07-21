package com.moru.backend.domain.log.dto;

import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.routine.dto.response.RoutineAppResponse;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record RoutineLogDetailResponse(
        UUID id,
        String routineTitle,
        boolean isSimple,
        boolean isCompleted,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Duration totalTime,
        String imageUrl,
        List<String> tagNames,
        List<RoutineStepLogDto> steps,
        List<RoutineAppResponse> apps,
        long completedStepCount,
        int totalStepCount
) {
    public static RoutineLogDetailResponse from (
            RoutineLog routineLog,
            RoutineSnapshot snapshot,
            String imageFullUrl,
            List<String> tagNames,
            List<RoutineStepLogDto> steps,
            List<RoutineAppResponse> apps
    ) {
        long completed = steps.stream().filter(RoutineStepLogDto::isCompleted).count();
        int total = steps.size();
        return new RoutineLogDetailResponse(
                routineLog.getId(),
                snapshot.getTitle(),
                snapshot.isSimple(),
                routineLog.isCompleted(),
                routineLog.getStartedAt(),
                routineLog.getEndedAt(),
                routineLog.getTotalTime(),
                imageFullUrl,
                tagNames,
                steps,
                apps,
                completed,
                total
        );
    }
}
