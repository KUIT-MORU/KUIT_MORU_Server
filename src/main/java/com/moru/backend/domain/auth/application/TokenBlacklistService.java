package com.moru.backend.domain.auth.application;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.TokenBlacklistRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private final JwtProvider jwtProvider;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    public void addBlacklist(String token) {
        Claims claims = jwtProvider.parseClaims(token);
        Date expiration = claims.getExpiration();
        long now = new Date().getTime();
        long remainingExpiration = expiration.getTime() - now;
        if (remainingExpiration > 0) {
            tokenBlacklistRepository.addBlacklist(token, remainingExpiration);
        }
    }

    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.isBlacklisted(token);
    }
} 