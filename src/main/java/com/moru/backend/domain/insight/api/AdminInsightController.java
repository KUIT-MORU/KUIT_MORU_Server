package com.moru.backend.domain.insight.api;

import com.moru.backend.domain.insight.application.UserInsightBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/insights")
@RequiredArgsConstructor
public class AdminInsightController {
    private final UserInsightBatchService userInsightBatchService;

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculateAllInsights() {
        userInsightBatchService.updateAllUserInsights();
        return ResponseEntity.ok().build();
    }
}
