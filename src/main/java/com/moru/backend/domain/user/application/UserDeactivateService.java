package com.moru.backend.domain.user.application;

import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.redis.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserDeactivateService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void deactivateUser(User user) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }

        user.deactivate();
        refreshTokenRepository.delete(user.getId().toString());
    }
}
