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
import com.moru.backend.global.util.S3Directory;
import com.moru.backend.global.util.S3Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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
    private final S3Service s3Service;

    public UserProfileResponse getProfile(User user) {
        if(!user.isActive()) {
            throw new CustomException(ErrorCode.USER_DEACTIVATED);
        }
        Long routineCount = (long) routineRepository.countByUserId(user.getId());
        FollowCountResponse followCount = followService.countFollow(user.getId());
        return UserProfileResponse.from(
                user,
                s3Service.getImageUrl(user.getProfileImageUrl()),
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
        FollowCountResponse followCount = followService.countFollow(targetUserId);

        // 실행 중인 루틴 조회 로직을 명확하게 정리
        RoutineListResponse currentRoutine = routineLogRepository.findTopByUserIdAndEndedAtIsNullOrderByStartedAtDesc(targetUserId)
                .map(activeLog -> RoutineListResponse.fromSnapshot(
                        activeLog.getRoutineSnapshot(),
                        s3Service.getImageUrl(activeLog.getRoutineSnapshot().getImageUrl())
                ))
                .orElse(null); // Optional이 비어있으면 null을 할당

        // 소유한 루틴 목록
        List<RoutineListResponse> routineLists = routineRepository.findAllByUserId(targetUserId).stream()
                .filter(Routine::isUserVisible)
                .map(r -> {
                    List<RoutineTag> tags = routineTagRepository.findByRoutine(r);
                    return RoutineListResponse.fromRoutine(
                            r,
                            s3Service.getImageUrl(r.getImageUrl()),
                            tags
                    );
                })
                .toList();

        return new OtherUserProfileResponse(
                isMe,
                targetUser.getNickname(),
                s3Service.getImageUrl(targetUser.getProfileImageUrl()),
                targetUser.getBio(),
                routineCount,
                followCount.followerCount().intValue(),
                followCount.followingCount().intValue(),
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

        // === 이미지 이동 처리 ===
        String imageKey = null;
        if(request.profileImageUrl() != null && !request.profileImageUrl().isBlank()) {
            imageKey = s3Service.moveToRealLocation(request.profileImageUrl(), S3Directory.PROFILE);
            user.setProfileImageUrl(imageKey);
        }

        return UserProfileResponse.from(
                user,
                s3Service.getImageUrl(user.getProfileImageUrl()),
                null,
                null,
                null);
    }
}
