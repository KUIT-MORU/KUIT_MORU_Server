package com.moru.backend.domain.insight.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moru.backend.domain.insight.dao.UserInsightRepository;
import com.moru.backend.domain.insight.domain.PaceGrade;
import com.moru.backend.domain.insight.domain.TimeSlot;
import com.moru.backend.domain.insight.domain.UserInsight;
import com.moru.backend.domain.user.domain.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserInsightService {
    private final RoutineInsightCalculator routineInsightCalculator;
    private final GlobalInsightCalculator globalInsightCalculator;
    private final ObjectMapper objectMapper;
    private final UserInsightRepository userInsightRepository;

    @Transactional
    public void calculateAndSaveUserInsight(User user, LocalDate startDate, LocalDate endDate) {
        // 실천률 계산
        double completionRate = routineInsightCalculator.calculateCompletionRate(user, startDate, endDate);
        // 주중 / 주말 평균 실천 루틴 수
        Map<String, Double> avgCounts = routineInsightCalculator.calculateWeekdayWeekendAvg(user, startDate, endDate);
        // 시간대별 실천 수
        Map<TimeSlot, Integer> countByTimeSlot = routineInsightCalculator.calculateCompletionCountByTimeSlot(user, startDate, endDate);

        // 인사이트 저장
        try {
            UserInsight insight = UserInsight.builder()
                    .user(user)
                    .paceGrade(PaceGrade.fromRate(completionRate))
                    .routineCompletionRate(completionRate)
                    .weekdayRoutineAvgCount(
                            avgCounts.getOrDefault("weekday", 0.0)
                    )
                    .weekendRoutineAvgCount(
                            avgCounts.getOrDefault("weekend", 0.0)
                    )
                    .routineCompletionCountByTimeSlotJson(objectMapper.writeValueAsString(countByTimeSlot))
                    .build();
            userInsightRepository.save(insight);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 오류", e);
        }
    }
}
