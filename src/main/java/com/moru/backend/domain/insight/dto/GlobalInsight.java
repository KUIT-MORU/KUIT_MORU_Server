package com.moru.backend.domain.insight.dto;

import java.io.Serializable;
import java.util.Map;

public record GlobalInsight(
        double averageCompletionRate,
        Map<String, Integer> completionRateDistribution,
        double overallWeekdayAvg,
        double overallWeekendAvg
) implements Serializable {}
