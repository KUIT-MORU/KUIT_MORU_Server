package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.TokenRefreshRequest;
import com.moru.backend.domain.auth.dto.TokenResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.RefreshTokenRepositoryImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshService {
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepositoryImpl refreshTokenRepository;

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();
        UUID userId = jwtProvider.getSubject(refreshToken);

        String storedRefreshToken = refreshTokenRepository.get(userId.toString());
        if(storedRefreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if(!refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        // Redis 갱신
        refreshTokenRepository.save(
                userId.toString(),
                newRefreshToken
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
