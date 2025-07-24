package com.moru.backend.domain.insight.dto;

import com.moru.backend.domain.insight.domain.PaceGrade;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description ="사용자 + 전체 인사이트 응답")
public record UserInsightResponse(
        @Schema(description = "루틴 페이스 등급")
        PaceGrade paceGrade,
        @Schema(description = "사용자 실천률 평균")
        double routineCompletionRate, // 실천률 소수점 단위

        // 전체 사용자 실천률 평균 및 실천률 분포
        @Schema(description = "전체 사용자 실천률 평균")
        double globalAverageRoutineCompletionRate,
        @Schema(description = "전체 사용자 실천률 분포")
        Map<String, Integer>completionDistribution,

        // 평균 루틴 실천 개수 (평일 / 주말)
        @Schema(description = "평일 / 주말 평균 루틴 실천 개수 사용자 vs 전체 비교")
        AverageRoutineCount averageRoutineCount, // 사용자 vs 전체 비교

        // 시간대별 루틴 실천 수
        @Schema(description = "시간대별 실천 루틴 수")
        Map<String, Integer> routineCompletionCountByTimeSlot
) {
    public record AverageRoutineCount(
            @Schema(description = "평일") RoutineAvg weekday,
            @Schema(description = "주말") RoutineAvg weekend
    ) {}

    public record RoutineAvg(
            @Schema(description = "사용자 평균") double user, // 사용자 하루 평균 실천 루틴 개수
            @Schema(description = "전체 사용자 평균") double overall // 전체 사용자 하루 평균 실천 루틴 개수
    ) {}
}
