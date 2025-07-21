package com.moru.backend.domain.user.application;


import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.social.application.FollowService;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.dao.UserFollowRepository;

import com.moru.backend.domain.social.dto.FollowCountResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.OtherUserProfileResponse;
import com.moru.backend.domain.user.dto.UserProfileRequest;
import com.moru.backend.domain.user.dto.UserProfileResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final RoutineRepository routineRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserRepository userRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineTagRepository routineTagRepository;
    private final FollowService followService;
    private final LikeService likeService;

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

    public OtherUserProfileResponse getOtherProfile(UUID targetUserId, User currentUser) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        boolean isMe = currentUser.getId().equals(targetUserId);

        int routineCount = routineRepository.countByUserId(targetUserId);
        int followerCount = (int) userFollowRepository.countByFollowingId(targetUserId);
        int followingCount = (int) userFollowRepository.countByFollowerId(targetUserId);

        //실행 중인 루틴
        RoutineLog activeLog = routineLogRepository.findActiveByUserId(targetUserId).orElse(null);
        RoutineListResponse currentRoutine = null;
        if (activeLog != null && activeLog.getRoutineSnapshot() != null) {
            RoutineSnapshot snapshot = activeLog.getRoutineSnapshot();
            List<String> tagNames = snapshot.getTagSnapshots().stream()
                    .map(RoutineTagSnapshot::getTagName)
                    .toList();
            new RoutineListResponse(
                    snapshot.getOriginalRoutineId(),
                    snapshot.getTitle(),
                    snapshot.getImageUrl(),
                    tagNames,
                    0,// 실행중인 루틴은 좋아요 개수 필요 x
                    null,
                    null
            );
        }

        List<Routine> routines = routineRepository.findAllByUserId(targetUserId)
                .stream()
                .filter(Routine::isUserVisible)
                .toList();
        List<RoutineListResponse> routineLists = routines.stream()
                .map(r -> {
                    List<RoutineTag> tags = routineTagRepository.findByRoutine(r);
                    int likeCount = likeService.countLikes(r.getId()).intValue();
                    return new RoutineListResponse(
                            r.getId(),
                            r.getTitle(),
                            r.getImageUrl(),
                            tags.stream().map(rt -> rt.getTag().getName()).toList(),
                            likeCount,
                            r.getCreatedAt(),
                            r.getRequiredTime()
                    );
                })
                .toList();

        return new OtherUserProfileResponse(
                isMe,
                targetUser.getNickname(),
                targetUser.getProfileImageUrl(),
                targetUser.getBio(),
                routineCount,
                followerCount,
                followingCount,
                currentRoutine,
                routineLists
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
