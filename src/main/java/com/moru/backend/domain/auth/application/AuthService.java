package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.*;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.jwt.JwtProvider;
import com.moru.backend.global.redis.RefreshTokenRepositoryImpl;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepositoryImpl refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public void signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            // 이미 회원가입된 이메일
        }
        if (userRepository.existsByNickname(request.nickname())) {
            // 이미 사용 중인 닉네임
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .gender(request.gender())
                .birthday(request.birthday())
                .bio(request.bio())
                .profileImageUrl(null)
                .status(true)
                .build();
        userRepository.save(user);
    }

    public TokenResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());

        if(user.isEmpty()) {
            // 존재하지 않는 이메일
        }
        if(!passwordEncoder.matches(request.password(), user.get().getPassword())) {
            // 잘못된 비밀번호
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

    public TokenResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.refreshToken();

        UUID userId = jwtProvider.getSubject(refreshToken);

        String storedRefreshToken = refreshTokenRepository.get(userId.toString());
        if(storedRefreshToken == null) {
            // 유효하지 않은 재발급 토큰
        }

        if(!refreshToken.equals(storedRefreshToken)) {
            // 리프레시 토큰 불일치
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

    public void logout(HttpServletRequest request) {
        String accessToken = jwtProvider.extractAccessToken(request);
        if(accessToken != null) {
            UUID userId = jwtProvider.getSubject(accessToken);
            refreshTokenRepository.delete(userId.toString());
        }
        SecurityContextHolder.clearContext();
    }
}
