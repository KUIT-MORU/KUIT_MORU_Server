package com.moru.backend.domain.insight.domain;

import java.time.LocalTime;

public enum TimeSlot {
    MORNING,
    AFTERNOON,
    EVENING,
    NIGHT;

    // LocalTime을 기반으로 해당 시간대 반환하기
    public static TimeSlot from(LocalTime time) {
        if(time == null) {
            return null;
        }

        int hour = time.getHour();

        if(hour >= 6 && hour < 12) {
            return MORNING;
        } else if(hour >= 12 && hour < 18) {
            return AFTERNOON;
        } else if(hour >= 18 && hour < 24) {
            return EVENING;
        } else {
            return NIGHT;
        }
    }
}
