package com.moru.backend.domain.auth.api;

import com.moru.backend.domain.auth.application.*;
import com.moru.backend.domain.auth.dto.*;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.moru.backend.global.jwt.JwtProvider;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SignupService signupService;
    private final LoginService loginService;
    private final RefreshService refreshService;
    private final LogoutService logoutService;
    private final JwtProvider jwtProvider;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid SignupRequest request) {
        signupService.signup(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(loginService.login(request));
    }

    @Operation(summary = "토큰 재발급 (access & refresh 모두 새로 만들어짐)")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid TokenRefreshRequest request) {
        return ResponseEntity.ok(refreshService.refreshToken(request));
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser User user, HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        logoutService.logout(user, accessToken);
        return ResponseEntity.ok().build();
    }
}
