package com.moru.backend.domain.user.application;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.dto.UserProfileRequest;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.redis.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserProfileResponse getProfile(User user) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }
        return UserProfileResponse.from(user);
    }


    public boolean isNicknameAvailable(String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_NICKNAME);
        }
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional
    public UserProfileResponse updateProfile(User user, UserProfileRequest request) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }

        if(request.nickname() != null && !request.nickname().trim().isBlank()) {
            if (userRepository.existsByNickname(request.nickname())) {
                throw new CustomException(ErrorCode.DUPLICATE_NICKNAME);
            }
            user.setNickname(request.nickname());
        }

        if(request.gender() != null) {
            user.setGender(request.gender());
        }

        if(request.birthday() != null) {
            user.setBirthday(request.birthday());
        }

        if(request.bio() != null && !request.bio().trim().isBlank()) {
            user.setBio(request.bio());
        }

        if(request.profileImageUrl() != null && !request.profileImageUrl().trim().isBlank()) {
            user.setProfileImageUrl(request.profileImageUrl());
        }

        return UserProfileResponse.from(user);
    }

    @Transactional
    public void deactivateUser(User user) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }

        user.deactivate();
        refreshTokenRepository.delete(user.getId().toString());
    }
}
