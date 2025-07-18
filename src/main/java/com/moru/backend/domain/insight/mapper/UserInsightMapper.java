package com.moru.backend.domain.insight.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moru.backend.domain.insight.domain.UserInsight;
import com.moru.backend.domain.insight.dto.UserInsightResponse;

import java.util.Map;

public class UserInsightMapper
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static UserInsightResponse toResponse(UserInsight userInsight) {
        return new UserInsightResponse(
                userInsight.getPaceGrade(),
                userInsight.getRoutineCompletionRate(),
                userInsight.getGlobalAverageRoutineCompletionRate(),
                parseJson(userInsight.getCompletionDistributionJson()),
                new UserInsightResponse.AverageRoutineCount(
                        new UserInsightResponse.RoutineAvg(
                                userInsight.getWeekdayRoutineAvgCount(),
                                userInsight.getOverallWeekdayRoutineAvgCount()
                        ),
                        new UserInsightResponse.RoutineAvg(
                                userInsight.getWeekendRoutineAvgCount(),
                                userInsight.getOverallWeekendRoutineAvgCount()
                        )
                ),
                parseJson(userInsight.getRoutineCompletionCountByTimeSlotJson())
        );
    }

    private static Map<String, Integer> parseJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            throw new RuntimeException("JSON 파싱 실패: " + json, e);
        }

    }
}
