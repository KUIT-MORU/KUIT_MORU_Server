package com.moru.backend.domain.insight.api;

import com.moru.backend.domain.insight.application.UserInsightService;
import com.moru.backend.domain.insight.dto.UserInsightResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
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

    @GetMapping("/")
    public ResponseEntity<UserInsightResponse> getUserInsight(@CurrentUser User user) {
        UserInsightResponse response = userInsightService.getInsight(user);
        return ResponseEntity.ok(response);
    }
}
