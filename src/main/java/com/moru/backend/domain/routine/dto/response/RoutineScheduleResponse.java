package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record RoutineScheduleResponse(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime time,
        Boolean alarmEnabled,
        String repeatType,
        List<DayOfWeek> daysToCreate
) {
    public static RoutineScheduleResponse from(RoutineSchedule routineSchedule, String repeatType, List<DayOfWeek> daysToCreate) {
        return new RoutineScheduleResponse(
                routineSchedule.getId(),
                routineSchedule.getDayOfWeek(),
                routineSchedule.getTime(),
                routineSchedule.isAlarmEnabled(),
                repeatType,
                daysToCreate
        );
    }
}