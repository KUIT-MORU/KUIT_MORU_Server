package com.moru.backend.domain.social.api;

import com.moru.backend.domain.social.application.FollowService;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.social.dto.FollowUserSummaryResponse;
import com.moru.backend.domain.social.dto.RoutineImportRequest;
import com.moru.backend.domain.social.dto.ScrappedRoutineSummaryResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.common.dto.ScrollResponse;
import com.moru.backend.global.validator.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/social")
public class SocialController {
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final FollowService followService;

    @Operation(summary = "좋아요 추가")
    @PostMapping("/{routineId}/likes")
    public ResponseEntity<Void> like(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        likeService.like(routineId, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "좋아요 삭제")
    @DeleteMapping("/{routineId}/likes")
    public ResponseEntity<Void> unlike(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        likeService.unlike(routineId, user);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "스크랩 추가")
    @PostMapping("/{routineId}/scraps")
    public ResponseEntity<Void> scrap(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        scrapService.scrap(routineId, user);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "스크랩 삭제")
    @DeleteMapping("/{routineId}/scraps")
    public ResponseEntity<Void> unscrap(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        scrapService.unscrap(routineId, user);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "스크랩 조회")
    @GetMapping("/scraps")
    public ResponseEntity<ScrollResponse<ScrappedRoutineSummaryResponse>> getScraps(
            @CurrentUser User user,
            @RequestParam(required = false) UUID lastScrapId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(scrapService.getScrappedRoutine(user, lastScrapId, limit));
    }

    @Operation(summary = "스크랩한 루틴을 내 루틴으로 불러오기")
    @PostMapping("/import")
    public ResponseEntity<Void> importSocial(
            @RequestBody RoutineImportRequest request,
            @CurrentUser User user
    ) {
        scrapService.importScrappedRoutines(user, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팔로우")
    @PostMapping("/following/{userId}")
    public ResponseEntity<Void> follow(
            @PathVariable UUID userId,
            @CurrentUser User user
    ) {
        System.out.println("user: "  + user);
        followService.follow(user, userId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "언팔로우")
    @DeleteMapping("/following/{userId}")
    public ResponseEntity<Void> unfollow(
            @PathVariable UUID userId,
            @CurrentUser User user
    ) {
        followService.unfollow(user, userId);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "팔로잉 조회")
    @GetMapping("/{userId}/following")
    public ResponseEntity<ScrollResponse<FollowUserSummaryResponse>> getFollowing(
            @PathVariable UUID userId,
            @CurrentUser User user,
            @RequestParam(required = false) UUID lastUserId,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(followService.getFollowingList(userId, user.getId(), lastUserId, limit));
    }

    @Operation(summary = "팔로워 조회")
    @GetMapping("/{userId}/follower")
    public ResponseEntity<ScrollResponse<FollowUserSummaryResponse>> getFollower(
            @PathVariable UUID userId,
            @CurrentUser User user,
            @RequestParam(required = false) UUID lastUserId,
            @RequestParam(defaultValue = "10") int limit
    ){
        return ResponseEntity.ok(followService.getFollowerList(userId, user.getId(), lastUserId, limit));
    }
}