package com.moru.backend.domain.insight.api;

import com.moru.backend.domain.insight.application.UserInsightService;
import com.moru.backend.domain.insight.dto.UserInsightResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/insights")
@RequiredArgsConstructor
public class UserInsightController {
    private final UserInsightService userInsightService;

    @Operation(summary = "인사이트 조회")
    @GetMapping("/")
    public ResponseEntity<UserInsightResponse> getUserInsight(@CurrentUser User user) {
        UserInsightResponse response = userInsightService.getInsight(user);
        return ResponseEntity.ok(response);
    }
}
