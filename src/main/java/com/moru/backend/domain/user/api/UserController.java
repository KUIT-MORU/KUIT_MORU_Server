package com.moru.backend.domain.user.api;

import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.domain.user.dto.UserProfileRequest;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.global.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "사용자 프로필 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        UUID userId = jwtProvider.getSubject(accessToken);
        UserProfileResponse response = userService.getProfile(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "닉네임 사용 가능 여부")
    @GetMapping("/nickname")
    public ResponseEntity<Map<String, Boolean>> checkNicknameDuplicate(@RequestParam("nickname") String nickname) {
        boolean available = userService.isNicknameAvailable(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @Operation(summary = "사용자 프로필 정보 수정")
    @PatchMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            HttpServletRequest request,
            @RequestBody @Valid UserProfileRequest userProfileRequest) {
        String accessToken = jwtProvider.extractAccessToken(request);
        UUID userId = jwtProvider.getSubject(accessToken);

        UserProfileResponse response = userService.updateProfile(userId, userProfileRequest);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 정보 삭제")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteUser(HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        UUID userId = jwtProvider.getSubject(accessToken);

        userService.deactivateUser(userId);
        return ResponseEntity.noContent().build(); // 204
    }
}
