package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.LoginRequest;
import com.moru.backend.domain.auth.dto.LoginResponse;
import com.moru.backend.domain.auth.dto.SignupRequest;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

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

    public LoginResponse login(LoginRequest request) {
        Optional<User> user = userRepository.findByEmail(request.email());

        if(user == null){
            // 존재하지 않는 이메일
        }
        if(!passwordEncoder.matches(request.password(), user.get().getPassword())) {
            // 잘못된 비밀번호
        }

        String accessToken = jwtProvider.createAccessToken(user.get().getId());
        String refreshToken = jwtProvider.createRefreshToken(user.get().getId());

        return new LoginResponse(accessToken, refreshToken);
    }
}
