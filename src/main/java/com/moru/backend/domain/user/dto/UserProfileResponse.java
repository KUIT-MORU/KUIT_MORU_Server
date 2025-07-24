package com.moru.backend.domain.user.dto;

import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.domain.user.domain.User;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponse (
    UUID id,
    String nickname,
    Gender gender,
    LocalDate birthday,
    String bio,
    String profileImageUrl,
    Long routineCount,
    Long followerCount,
    Long followingCount
) {
    public static UserProfileResponse from(User user,
                                           String imageFullUrl,
                                           Long routineCount,
                                           Long followerCount,
                                           Long followingCount) {
        return new UserProfileResponse(
                user.getId(),
                user.getNickname(),
                user.getGender(),
                user.getBirthday(),
                user.getBio(),
                imageFullUrl,
                routineCount,
                followerCount,
                followingCount
        );
    }
}
