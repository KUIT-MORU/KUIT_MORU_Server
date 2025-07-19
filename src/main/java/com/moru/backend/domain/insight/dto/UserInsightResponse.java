package com.moru.backend.domain.insight.dto;

import com.moru.backend.domain.insight.domain.PaceGrade;

import java.util.Map;

public record UserInsightResponse(
        // 루틴 페이스
        PaceGrade paceGrade,
        double routineCompletionRate, // 실천률 소수점 단위

        // 전체 사용자 실천률 평균 및 실천률 분포
        double globalAverageRoutineCompletionRate, // 전체 평균 실천률
        Map<String, Integer>completionDistribution,

        // 평균 루틴 실천 개수 (평일 / 주말)
        AverageRoutineCount averageRoutineCount, // 사용자 vs 전체 비교

        // 시간대별 루틴 실천 수
        Map<String, Integer> routineCompletionCountByTimeSlot
) {
    public record AverageRoutineCount(
            RoutineAvg weekday,
            RoutineAvg weekend
    ) {}

    public record RoutineAvg(
            double user, // 사용자 하루 평균 실천 루틴 개수
            double overall // 전체 사용자 하루 평균 실천 루틴 개수
    ) {}
}
