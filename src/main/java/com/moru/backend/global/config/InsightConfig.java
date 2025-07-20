package com.moru.backend.global.config;

public class InsightConfig {
    // 인사이트 계산 대상 기간
    public static final int INSIGHT_DAYS_RANGE = 7;

    // 전역 인사이트가 조회 당일에 없는 경우, 얼마나 과거까지 확인할 것인가
    public static final int MAX_LOOKBACK_DAYS = 7;


    private InsightConfig() {
        // 상수 클래스 인스턴스화 방지
    }
}
