package com.moru.backend.domain.insight.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InsightBatchScheduler {
    private final UserInsightBatchService userInsightBatchService;

    /**
     * 매일 새벽 3시에 인사이트 전체 갱신
     */
    @Scheduled(cron = "0 0 3 * * *") // 초 분 시 일 월 요일
    public void scheduledInsightUpdate() {
        log.info("[InsightBatch] 사용자 인사이트 갱신 시작");
        userInsightBatchService.updateAllUserInsights();
        log.info("[InsightBatch] 사용자 인사이트 갱신 종료");
    }
}
