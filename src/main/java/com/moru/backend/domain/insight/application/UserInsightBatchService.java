package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static com.moru.backend.global.config.InsightConfig.INSIGHT_DAYS_RANGE;

@Service
@RequiredArgsConstructor
public class UserInsightBatchService {
    private final UserRepository userRepository;
    private final UserInsightService userInsightService;
    private final GlobalInsightService globalInsightService;

    public void updateAllUserInsights() {
        List<User> activeUsers = userRepository.findAllByStatusTrue();

        // 최근 7일 간의 기록을 바탕으로 인사이트 계산
        LocalDate startDate = LocalDate.now().minusDays(INSIGHT_DAYS_RANGE);
        LocalDate endDate = LocalDate.now().minusDays(1); // 오늘은 제외

        // 개인 인사이트 계산
        for (User user : activeUsers) {
            userInsightService.calculateAndSaveUserInsight(user, startDate, endDate);
        }

        // 전체 인사이트 계산
        globalInsightService.recalculateAndCacheGlobalInsight(startDate);
    }
}
