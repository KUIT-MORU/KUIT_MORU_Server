package com.moru.backend.domain.social.api;

import com.moru.backend.domain.social.application.FollowService;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.social.dto.ScrappedRoutineSummaryResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/social")
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
    public ResponseEntity<List<ScrappedRoutineSummaryResponse>> getScraps(@CurrentUser User user) {
        return ResponseEntity.ok(scrapService.getScrappedRoutine(user));
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
}