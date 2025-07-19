package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.dao.UserInsightRepository;
import com.moru.backend.domain.insight.domain.UserInsight;
import com.moru.backend.domain.insight.dto.GlobalInsight;
import com.moru.backend.global.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class GlobalInsightCalculator {
    private final UserInsightRepository userInsightRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public void recalculateAndCacheGlobalInsight(LocalDate startDate) {
        LocalDateTime updatedAtStart = startDate.atStartOfDay(); // 00:00:00
        List<UserInsight> insights = userInsightRepository.findByUpdatedAtAfter(updatedAtStart);
        if(insights.isEmpty()) { return; }

        // 전체 평균 실천률
        double averageCompletionRate = getAverageCompletionRate(insights);

        // 실천률 분포
        Map<String, Integer> completionRateDistribution = getCompletionRateDistribution(insights);

        // 평일 / 주말 루틴 평균
        Map<String, Double> overallWeekdayWeekendAvg = getOverallWeekdayWeekendAvg(insights);

        GlobalInsight globalInsight = new GlobalInsight(
                averageCompletionRate,
                completionRateDistribution,
                overallWeekdayWeekendAvg.getOrDefault("weekday", 0.0),
                overallWeekdayWeekendAvg.getOrDefault("weekend", 0.0)
        );

        String redisKey = RedisKeyUtil.globalInsightKey(LocalDate.now());
        Duration ttl = Duration.ofDays(30); // 30일 후 자동 만료

        redisTemplate.opsForValue().set(redisKey, globalInsight, ttl);
    }

    /**
     * 전체 사용자 평균 실천률 계산 (최근 7일 이내로 계산된 인사이트 대상)
     */
    public double getAverageCompletionRate(List<UserInsight> insights) {
        return insights.stream()
                .mapToDouble(UserInsight::getRoutineCompletionRate)
                .average()
                .orElse(0.0);
    }

    /**
     * 실천률 분포 계산 (0-10%, 10-20%, ..., 90-100%)
     * 결과 예시: {"0-10": 3, "10-20": 7, ..., "90-100": 11}
     */
    public Map<String, Integer> getCompletionRateDistribution(List<UserInsight> insights) {
        Map<String, Integer> distribution = new TreeMap<>();

        for(UserInsight userInsight : insights) {
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
    public Map<String, Double> getOverallWeekdayWeekendAvg(List<UserInsight> insights) {
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
