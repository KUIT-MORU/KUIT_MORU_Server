package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.LoginRequest;
import com.moru.backend.domain.auth.dto.SignupRequest;
import com.moru.backend.domain.auth.dto.TokenResponse;
import com.moru.backend.domain.user.application.UserFavoriteTagService;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserRole;
import com.moru.backend.domain.user.dto.FavoriteTagRequest;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.S3Directory;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LoginService loginService;

    @Transactional
    public TokenResponse signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .status(true)
                .build();

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest(request.email(), request.password());

        return loginService.login(loginRequest).token();
    }
}
