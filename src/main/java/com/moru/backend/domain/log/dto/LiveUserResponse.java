package com.moru.backend.domain.log.dto;
import java.util.UUID;

public record LiveUserResponse(
    UUID userId,
    String nickname,
    String profileImageUrl,
    String motivationTag
) {} 