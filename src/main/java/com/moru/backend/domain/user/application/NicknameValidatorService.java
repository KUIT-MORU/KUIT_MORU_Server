package com.moru.backend.domain.user.application;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NicknameValidatorService {
    private final UserRepository userRepository;

    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }
        return !userRepository.existsByNickname(nickname);
    }
}
