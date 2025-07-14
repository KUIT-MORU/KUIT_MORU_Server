package com.moru.backend.domain.social.application;

import com.moru.backend.domain.social.dao.UserFollowRepository;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.social.dto.FollowCountResponse;
import com.moru.backend.domain.social.dto.FollowUserSummaryResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final UserRepository userRepository;
    private final UserFollowRepository userFollowRepository;

    public FollowCountResponse countFollow(UUID userId) {
        Long followingCount = userFollowRepository.countByFollowerId(userId);
        Long followCount = userFollowRepository.countByFollowingId(userId);

        return new FollowCountResponse(followingCount, followCount);
    }

    public boolean isAlreadyFollowing(UUID followerId, UUID followingId) {
        return userFollowRepository.existsByFollowerIdAndFollowingId(followerId, followingId);
    }

    @Transactional
    public void follow(User me, UUID targetUserId) {
        if(me.getId().equals(targetUserId)) {
            throw new CustomException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if(isAlreadyFollowing(me.getId(), targetUserId)) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        UserFollow userFollow = UserFollow.builder()
                .follower(me)
                .following(target)
                .build();

        userFollowRepository.save(userFollow);
    }

    @Transactional
    public void unfollow(User me, UUID targetUserId) {
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        UserFollow follow = userFollowRepository.findByFollowerIdAndFollowingId(me.getId(), targetUserId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_FOUND));

        userFollowRepository.delete(follow);
    }

    public List<FollowUserSummaryResponse> getFollowingList(UUID userId) {
        List<UserFollow> followings = userFollowRepository.findAllByFollowerId(userId);

        return followings.stream()
                .map(relation -> {
                    User following = relation.getFollowing();
                    return FollowUserSummaryResponse.from(following, true);
                })
                .toList();
    }

    public List<FollowUserSummaryResponse> getFollowerList(UUID userId) {
        List<UserFollow> followers = userFollowRepository.findAllByFollowingId(userId);

        return followers.stream()
                .map(relation -> {
                    User follower = relation.getFollower();
                    boolean isFollow = userFollowRepository
                            .existsByFollowerIdAndFollowingId(
                                    userId,
                                    follower.getId()
                            );
                    return FollowUserSummaryResponse.from(follower, isFollow);
                })
                .toList();
    }
}
