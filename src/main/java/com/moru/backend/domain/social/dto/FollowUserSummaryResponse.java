package com.moru.backend.domain.social.dto;

import com.moru.backend.domain.user.domain.User;

import java.util.UUID;

public record FollowUserSummaryResponse(
        UUID userId,
        String profileImageUrl,
        String nickname,
        String bio,
        boolean isFollow
) {
    public static FollowUserSummaryResponse from(User user, String imageFullUrl, boolean isFollow) {
        return new FollowUserSummaryResponse(
                user.getId(),
                imageFullUrl,
                user.getNickname(),
                user.getBio(),
                isFollow
        );
    }
}
