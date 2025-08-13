package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.log.application.RoutineLogService;
import com.moru.backend.domain.log.dto.LiveUserResponse;
import com.moru.backend.domain.routine.application.RoutineRecommendService;
import com.moru.backend.domain.routine.dto.response.RecommendFeedResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Tag(name = "루틴 피드/추천", description = "루틴 피드 및 추천 관련 API")
public class RoutineFeedController {
    private final RoutineRecommendService routineRecommendService;
    private final RoutineLogService routineLogService;

    @Operation(summary = "루틴 피드 추천", description = "루틴 피드 추천을 받습니다.")
    @GetMapping("/recommend/feed")
    public ResponseEntity<RecommendFeedResponse> getRecommendFeed(@CurrentUser User currentUser) {
        RecommendFeedResponse response = routineRecommendService.getRecommendFeed(currentUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "모루 라이브 기능", description = "랜덤 유저 총 10명의 이미지, 닉네임, 실행중인 태그 중 첫번째를 반환한다")
    @GetMapping("/live-users")
    public List<LiveUserResponse> getLiveUsers(@RequestParam(defaultValue = "10") int count) {
        return routineLogService.getLiveUsers(count);
    }
}
