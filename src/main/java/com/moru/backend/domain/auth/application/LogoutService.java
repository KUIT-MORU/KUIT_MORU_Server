package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.application.TokenBlacklistService;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistService tokenBlacklistService;

    public void logout(User user, String accessToken) {
        // // 저장된 refresh Token 삭제
        // refreshTokenRepository.delete(user.getId().toString()); 
        // // access token 블랙리스트에 저장
        // tokenBlacklistService.addBlacklist(accessToken);
        if (user == null || user.getId() == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        refreshTokenRepository.delete(user.getId().toString());
        tokenBlacklistService.addBlacklist(accessToken);
    }
}
