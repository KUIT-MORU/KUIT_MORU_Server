package com.moru.backend.domain.user.api;

import com.moru.backend.domain.user.application.*;

import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.*;

import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;
    private final NicknameValidatorService nicknameValidatorService;
    private final UserDeactivateService userDeactivateService;

    private final UserFavoriteTagService userFavoriteTagService;
    private final UserFcmService userFcmService;

    @Operation(summary = "사용자 프로필 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser User user) {
        UserProfileResponse response = userProfileService.getProfile(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "다른 사용자 프로필 정보 조회")
    @GetMapping("/{userId}")
    public ResponseEntity<OtherUserProfileResponse> getProfile(
            @PathVariable UUID userId,
            @CurrentUser User user) {
        OtherUserProfileResponse response = userProfileService.getOtherProfile(userId, user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 사용 가능 여부")

    @GetMapping("/nickname/{nickname}")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@PathVariable String nickname) {
        boolean available = nicknameValidatorService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @Operation(summary = "사용자 프로필 정보 수정")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @CurrentUser User user,
            @RequestBody @Valid UserProfileRequest userProfileRequest) {
        UserProfileResponse response = userProfileService.updateProfile(user, userProfileRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 정보 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(@CurrentUser User user) {
        userDeactivateService.deactivateUser(user);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "관심 태그 추가")
    @PostMapping("/favorite-tag")
    public ResponseEntity<?> addFavoriteTag(
            @CurrentUser User user,
            @RequestBody @Valid FavoriteTagRequest request
    ) {
        userFavoriteTagService.addFavoriteTag(user, request);
        return ResponseEntity.ok().body("관심 태그가 성공적으로 추가되었습니다.");
    }

    @Operation(summary = "관심 태그 삭제")
    @DeleteMapping("/favorite-tag/{tagId}")
    public ResponseEntity<?> removeFavoriteTag(
            @CurrentUser User user,
            @PathVariable UUID tagId
    ) {
        userFavoriteTagService.removeFavoriteTag(user, tagId);
        return ResponseEntity.ok().body("관심 태그가 삭제되었습니다.");
    }

    @Operation(summary = "관심 태그 조회")
    @GetMapping("/favorite-tag")
    public ResponseEntity<List<FavoriteTagResponse>> getFavoriteTags(@CurrentUser User user) {
        List<FavoriteTagResponse> response = userFavoriteTagService.getFavoriteTags(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "FCM 토큰 등록")
    @PostMapping("/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @CurrentUser User user,
            @RequestBody FcmTokenRequest request
    ) {
        userFcmService.updateFcmToken(user.getId(), request.fcmToken());
        return ResponseEntity.noContent().build();
    }
}
