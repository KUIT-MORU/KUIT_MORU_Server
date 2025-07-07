package com.moru.backend.domain.auth.dto;

public record TokenRefreshRequest (
        String refreshToken
) {}
