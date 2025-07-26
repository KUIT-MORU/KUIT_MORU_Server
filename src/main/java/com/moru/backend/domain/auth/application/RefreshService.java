package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.TokenRefreshRequest;
import com.moru.backend.domain.auth.dto.TokenResponse;
import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.domain.user.domain.UserRole;
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
    private final UserService userService;

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();
        UUID userId = jwtProvider.getSubject(refreshToken);

        if(userId == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String storedRefreshToken = refreshTokenRepository.get(userId.toString());
        if(storedRefreshToken == null) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if(!refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
        }

        UserRole role = userService.getUserById(userId).getRole();

        String newAccessToken = jwtProvider.createAccessToken(userId, role);
        String newRefreshToken = jwtProvider.createRefreshToken(userId, role);

        // Redis 갱신
        refreshTokenRepository.save(
                userId.toString(),
                newRefreshToken
        );

        return new TokenResponse(newAccessToken, newRefreshToken);
    }
}
