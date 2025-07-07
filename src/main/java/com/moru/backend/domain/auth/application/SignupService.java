package com.moru.backend.domain.auth.application;

import com.moru.backend.domain.auth.dto.SignupRequest;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new CustomException(ErrorCode.USER_EMAIL_ALREADY_EXISTS);
        }
        if(userRepository.existsByNickname(request.nickname())) {
            throw new CustomException(ErrorCode.USER_NICKNAME_ALREADY_EXISTS);
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
}
