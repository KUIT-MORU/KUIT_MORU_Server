package com.moru.backend.domain.social.dto;

public record FollowCountResponse(
        Long followingCount,
        Long followerCount
) {}
