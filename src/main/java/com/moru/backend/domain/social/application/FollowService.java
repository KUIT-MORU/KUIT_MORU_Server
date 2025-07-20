package com.moru.backend.domain.social.application;

import com.moru.backend.domain.social.dao.UserFollowRepository;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.social.dto.FollowCountResponse;
import com.moru.backend.domain.social.dto.FollowUserSummaryResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.common.dto.ScrollResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ScrollResponse<FollowUserSummaryResponse> getFollowingList(
            UUID targetUserId, UUID loginUserId,
            UUID lastUserId, int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        List<UserFollow> followings = userFollowRepository.findFollowingsByCursor(targetUserId, lastUserId, pageable);

        Set<UUID> followingIdsByLoginUser = getFollowingIdSet(loginUserId);

        List<FollowUserSummaryResponse> result = followings.stream()
                .map(relation -> {
                    User following = relation.getFollowing();
                    boolean isFollowing = followingIdsByLoginUser.contains(following.getId());
                    return FollowUserSummaryResponse.from(following, isFollowing);
                })
                .toList();
        boolean hasNext = result.size() == limit;
        return ScrollResponse.of(result, hasNext);
    }

    public ScrollResponse<FollowUserSummaryResponse> getFollowerList(
            UUID targetUserId, UUID loginUserId,
            UUID lastUserId, int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        List<UserFollow> followers = userFollowRepository.findFollowersByCursor(targetUserId, lastUserId, pageable);

        Set<UUID> followingIdsByLoginUser = getFollowingIdSet(loginUserId);

        List<FollowUserSummaryResponse> result = followers.stream()
                .map(relation -> {
                    User follower = relation.getFollower();
                    boolean isFollowing = followingIdsByLoginUser.contains(follower.getId());
                    return FollowUserSummaryResponse.from(follower, isFollowing);
                })
                .toList();
        boolean hasNext = result.size() == limit;
        return ScrollResponse.of(result, hasNext);
    }

    private Set<UUID> getFollowingIdSet(UUID loginUserId) {
        return userFollowRepository.findAllByFollowerId(loginUserId)
                .stream()
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toSet());
    }
}
