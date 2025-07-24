package com.moru.backend.domain.social.dto;

import java.util.UUID;

public record FollowCursor(
        String nickname,
        UUID userId
) {}
