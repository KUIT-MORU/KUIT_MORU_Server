package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.dao.UserInsightRepository;
import com.moru.backend.domain.insight.domain.UserInsight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class GlobalInsightCalculator {
    private final UserInsightRepository userInsightRepository;

    /**
     * 전체 사용자 평균 실천률 계산 (최근 7일 이내로 계산된 인사이트 대상)
     */
    public double getAverageCompletionRate() {
        List<UserInsight> recentInsights = userInsightRepository.findByUpdatedAtAfter(LocalDate.now().minusDays(7));
        return recentInsights.stream()
                .mapToDouble(UserInsight::getRoutineCompletionRate)
                .average()
                .orElse(0.0);
    }

    /**
     * 실천률 분포 계산 (0-10%, 10-20%, ..., 90-100%)
     * 결과 예시: {"0-10": 3, "10-20": 7, ..., "90-100": 11}
     */
    public Map<String, Integer> getCompletionRateDistribution() {
        List<UserInsight> recentInsights = userInsightRepository.findByUpdatedAtAfter(LocalDate.now().minusDays(7));
        Map<String, Integer> distribution = new TreeMap<>();

        for(UserInsight userInsight : recentInsights) {
            double rate = userInsight.getRoutineCompletionRate() * 100;
            int lower = ((int) rate / 10) * 10;
            int upper = lower + 10;
            if(upper > 100) {
                upper = 100;
            }
            String key = String.format("%d-%d", lower, upper);
            distribution.put(key, distribution.getOrDefault(key, 0) + 1);
        }

        return distribution;
    }

    /**
     * 주중 / 주말 실천 전체 평균 개수 계산
     */
    public Map<String, Double> getOverallWeekdayWeekendAvg() {
        List<UserInsight> insights = userInsightRepository.findByUpdatedAtAfter(LocalDate.now().minusDays(7));

        double weekdaySum = 0.0;
        double weekendSum = 0.0;
        int weekdayCount = 0;
        int weekendCount = 0;

        for(UserInsight insight : insights) {
            if(insight.getWeekdayRoutineAvgCount() != null) {
                weekdaySum += insight.getWeekdayRoutineAvgCount();
                weekdayCount += 1;
            }
            if(insight.getWeekendRoutineAvgCount() != null) {
                weekendSum += insight.getWeekendRoutineAvgCount();
                weekendCount += 1;
            }
        }

        double weekdayAvg = weekdayCount == 0 ? 0.0 : (weekdaySum / weekdayCount);
        double weekendAvg = weekendCount == 0 ? 0.0 : (weekendSum / weekendCount);

        return Map.of(
                "weekday", weekdayAvg,
                "weekend", weekendAvg
        );
    }
}
