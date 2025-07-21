package com.moru.backend.domain.user.dto;

import com.moru.backend.domain.routine.dto.response.RoutineListResponse;

import java.util.List;

public record OtherUserProfileResponse(
        boolean isMe,
        String nickname,
        String profileImageUrl,
        String bio,
        int routineCount,
        int followerCount,
        int followingCount,
        RoutineListResponse currentRoutine,
        List<RoutineListResponse> routines
) {
}
