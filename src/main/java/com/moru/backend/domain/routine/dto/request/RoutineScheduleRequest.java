package com.moru.backend.domain.routine.dto.request;

import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record RoutineScheduleRequest(
        DayOfWeek dayOfWeek,
        LocalTime time,
        Boolean alarmEnabled,
        String repeatType,
        List<DayOfWeek> customDays
) {}