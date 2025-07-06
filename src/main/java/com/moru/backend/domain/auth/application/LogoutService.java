package com.moru.backend.domain.auth.application;

import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LogoutService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public void logout(HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        if(accessToken != null) {
            UUID userId = jwtProvider.getSubject(accessToken);
            refreshTokenRepository.delete(userId.toString());
        }
        SecurityContextHolder.clearContext();
    }
}
