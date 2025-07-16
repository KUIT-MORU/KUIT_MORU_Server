package com.moru.backend.domain.routine.dto.request;

import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record RoutineScheduleRequest(
        DayOfWeek dayOfWeek,
        @Schema(description = "루틴 시간(HH:mm:ss)", example = "14:30:00")
        LocalTime time,
        Boolean alarmEnabled,
        String repeatType,
        List<DayOfWeek> customDays
) {}