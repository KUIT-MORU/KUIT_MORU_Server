package com.moru.backend.domain.user.api;

import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.global.jwt.JwtProvider;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원 프로필 정보 조회")
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        UUID userId = jwtProvider.getSubject(accessToken);
        UserProfileResponse response = userService.getMyProfile(userId);

        return ResponseEntity.ok(response);
    }
//
//    @Operation(summary = "닉네임 중복 여부 확인")
//    @GetMapping("/nickname")
//    public ResponseEntity<Boolean> getCurrentUserNickname() {}
}
