package com.moru.backend.domain.routine.dto.response;

public record LiveUserResponse(
    String username,
    String profileImageUrl,
    String motivationTag,
    String feelUrl
) {} 