package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.domain.UserInsight;

import java.util.UUID;

public interface UserInsightService {
    // 특정 유저의 인사이트 생성 또는 갱신
    void updateUserInsight(UUID userId);

    // 전체 유저의 인사이트 일괄 갱신 (관리자)
    void updateAllUserInsights();

    // 유저 인사이트 조회 (없으면 Lazy 계산 포함 가능)
    UserInsight getUserInsight(UUID userId);
}
