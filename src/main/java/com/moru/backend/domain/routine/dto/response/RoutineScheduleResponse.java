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
        String repeatType, // 주말마다, 평일만, 매일
        List<DayOfWeek> customDays // 임의로 설정할때
) {
    public static RoutineScheduleResponse from(RoutineSchedule routineSchedule, String repeatType, List<DayOfWeek> customDays) {
        return new RoutineScheduleResponse(
                routineSchedule.getId(),
                routineSchedule.getDayOfWeek(),
                routineSchedule.getTime(),
                routineSchedule.isAlarmEnabled(),
                repeatType,
                customDays
        );
    }
}