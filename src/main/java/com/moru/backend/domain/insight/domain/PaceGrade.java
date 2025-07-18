package com.moru.backend.domain.insight.domain;

import java.util.Arrays;

public enum PaceGrade {
    WALKING("잠시 걷는 중", 0.0, 0.3),
    INTERMITTENT("간헐적 루틴러", 0.3, 0.7),
    PACE_MAKER("루틴 페이스 메이커", 0.7, 1.0);

    private final String label;
    private final Double minRate;
    private final Double maxRate;

    PaceGrade(String label, Double minRate, Double maxRate) {
        this.label = label;
        this.minRate = minRate;
        this.maxRate = maxRate;
    }

    public String getLabel() {
        return label;
    }

    public static PaceGrade fromRate(double rate) {
        return Arrays.stream(values())
                .filter(g -> rate >= g.minRate && rate < g.maxRate)
                .findFirst()
                .orElse(PaceGrade.WALKING); // 기본값은 가장 낮은 걸로
    }
}
