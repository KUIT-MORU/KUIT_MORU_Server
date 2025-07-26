package com.moru.backend.domain.insight.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moru.backend.domain.insight.dao.UserInsightRepository;
import com.moru.backend.domain.insight.domain.PaceGrade;
import com.moru.backend.domain.insight.domain.TimeSlot;
import com.moru.backend.domain.insight.domain.UserInsight;
import com.moru.backend.domain.insight.dto.GlobalInsight;
import com.moru.backend.domain.insight.dto.UserInsightResponse;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static com.moru.backend.global.config.InsightConfig.INSIGHT_DAYS_RANGE;

@Service
@RequiredArgsConstructor
public class UserInsightService {
    private final RoutineInsightCalculator routineInsightCalculator;
    private final ObjectMapper objectMapper;
    private final UserInsightRepository userInsightRepository;
    private final GlobalInsightService globalInsightService;

    public UserInsightResponse getInsight(User user) {
        LocalDate startDate = LocalDate.now().minusDays(INSIGHT_DAYS_RANGE);
        LocalDate endDate = LocalDate.now().minusDays(1); // 오늘은 제외

        // 유저 개인 인사이트 조회 또는 계산
        UserInsight userInsight = userInsightRepository.findByUserId(user.getId())
                .orElseGet(() -> calculateAndSaveUserInsight(user, startDate, endDate));

        // 전체 인사이트 (Redis에서 조회)
        GlobalInsight globalInsight = globalInsightService.getOrCalculateGlobalInsight();

        return new UserInsightResponse(
                userInsight.getPaceGrade(),
                userInsight.getRoutineCompletionRate(),
                globalInsight.averageCompletionRate(),
                globalInsight.completionRateDistribution(),
                new UserInsightResponse.AverageRoutineCount(
                        new UserInsightResponse.RoutineAvg(
                                userInsight.getWeekdayRoutineAvgCount(),
                                globalInsight.overallWeekdayAvg()
                        ),
                        new UserInsightResponse.RoutineAvg(
                                userInsight.getWeekendRoutineAvgCount(),
                                globalInsight.overallWeekendAvg()
                        )
                ),
                userInsight.getRoutineCompletionCountByTimeSlotAsMap()
        );
    }

    @Transactional
    public UserInsight calculateAndSaveUserInsight(User user, LocalDate startDate, LocalDate endDate) {
        // 실천률 계산
        double completionRate = routineInsightCalculator.calculateCompletionRate(user, startDate, endDate);
        // 주중 / 주말 평균 실천 루틴 수
        Map<String, Double> avgCounts = routineInsightCalculator.calculateWeekdayWeekendAvg(user, startDate, endDate);
        // 시간대별 실천 수
        Map<TimeSlot, Integer> countByTimeSlot = routineInsightCalculator.calculateCompletionCountByTimeSlot(user, startDate, endDate);

        // 이미 있는지 확인
        Optional<UserInsight> userInsight = userInsightRepository.findByUserId(user.getId());

        // 인사이트 저장
        try {
            if(userInsight.isPresent()) {
                // 이미 있으면 업데이트
                UserInsight insight = userInsight.get();
                insight.setPaceGrade(PaceGrade.fromRate(completionRate));
                insight.setRoutineCompletionRate(completionRate);
                insight.setWeekdayRoutineAvgCount(avgCounts.getOrDefault("weekday", 0.0));
                insight.setWeekendRoutineAvgCount(avgCounts.getOrDefault("weekend", 0.0));
                insight.setRoutineCompletionCountByTimeSlotJson(objectMapper.writeValueAsString(countByTimeSlot));
                insight.setUpdatedAt(LocalDateTime.now());
                return insight;
            }
            // 없으면 새로 저장
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
            return insight;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 오류", e);
        }
    }
}
