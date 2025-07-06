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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        UUID userId = user.getId();
        String accessToken = jwtProvider.createAccessToken(userId);
        String refreshToken = jwtProvider.createRefreshToken(userId);

        refreshTokenRepository.save(
                userId.toString(),
                refreshToken
        );

        return new TokenResponse(accessToken, refreshToken);
    }

    /**
     * SecurityContext에서 현재 로그인된 User을 찾아서 반환
     * @throws CustomException 인증되지 않았거나 사용자를 찾을 수 없으면 에러
     */
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UUID)) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        UUID userId = (UUID) principal;
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
