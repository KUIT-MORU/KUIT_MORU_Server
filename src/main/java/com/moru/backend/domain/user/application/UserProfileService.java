package com.moru.backend.domain.user.application;


import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.social.application.FollowService;
import com.moru.backend.domain.social.dao.UserFollowRepository;

import com.moru.backend.domain.social.dto.FollowCountResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.UserProfileRequest;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final RoutineRepository routineRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final FollowService followService;

    public UserProfileResponse getProfile(User user) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }
        Long routineCount = (long) routineRepository.countByUserId(user.getId());
        FollowCountResponse followCount = followService.countFollow(user.getId());
        return UserProfileResponse.from(
                user,
                routineCount,
                followCount.followerCount(),
                followCount.followingCount()
        );
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

        return UserProfileResponse.from(user, null, null, null);
    }
}
