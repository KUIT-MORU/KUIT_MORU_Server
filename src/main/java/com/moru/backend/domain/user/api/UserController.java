package com.moru.backend.domain.user.api;

import com.moru.backend.domain.user.application.NicknameValidatorService;
import com.moru.backend.domain.user.application.UserDeactivateService;
import com.moru.backend.domain.user.application.UserProfileService;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.UserProfileRequest;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.global.validator.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserProfileService userProfileService;
    private final NicknameValidatorService nicknameValidatorService;
    private final UserDeactivateService userDeactivateService;

    @Operation(summary = "사용자 프로필 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser User user) {
        UserProfileResponse response = userProfileService.getProfile(user);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 사용 가능 여부")
    @GetMapping("/nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
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
}
