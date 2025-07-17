package com.moru.backend.domain.insight.application.analyzer;

import com.moru.backend.domain.insight.dto.CompletionRateResult;

import java.util.UUID;

public interface CompletionRateAnalyzer {
    CompletionRateResult analyze(UUID userId);
}
