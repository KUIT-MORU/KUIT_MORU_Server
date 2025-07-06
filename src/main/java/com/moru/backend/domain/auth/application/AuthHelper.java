package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.global.jwt.JwtProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHelper {
    private final JwtProvider jwtProvider;

    public UUID extractUserId(HttpServletRequest request) {
        String token = jwtProvider.extractAccessToken(request);
        return jwtProvider.getSubject(token);
    }
}
