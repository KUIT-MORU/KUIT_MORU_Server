package com.moru.backend.domain.routine.dto.request;

import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

public record RoutineScheduleRequest(
        @Schema(description = "반복 타입", example = "CUSTOM")
        String repeatType,
        @Schema(description = "반복 요일", example = "[\"MON\", \"WED\"]")
        List<DayOfWeek> daysToCreate,
        @Schema(description = "루틴 시간(HH:mm:ss)", example = "14:30:00")
        LocalTime time,
        @Schema(description = "알람 설정 여부", example = "true")
        Boolean alarmEnabled
) {}