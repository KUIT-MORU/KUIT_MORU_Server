package com.moru.backend.domain.insight.dto;

import com.moru.backend.domain.insight.domain.UserInsight;
import lombok.Data;

import java.util.Map;

@Data
public class CompletionRateResult {
    // 완료율 분석 결과
    private double userCompletionRate;              // 내 실천률
    private double globalAverageCompletionRate;     // 전체 평균
    // 분포 구조: {0: 2, 10: 4, ..., 90: 8, 100: 1} 형태
    private Map<Integer, Integer> distributionMap; // 10% 단위 실천률 분포
}
