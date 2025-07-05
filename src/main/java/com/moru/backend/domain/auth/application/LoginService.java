package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.LoginRequest;
import com.moru.backend.domain.auth.dto.TokenResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());

        if(user.isEmpty()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }
        if(!passwordEncoder.matches(request.password(), user.get().getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        UUID userId = user.get().getId();
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        refreshTokenRepository.save(
                userId.toString(),
                refreshToken
        );

        return new TokenResponse(accessToken, refreshToken);
    }
}
