package com.moru.backend.domain.auth.dto;

public record LoginResponse (
        TokenResponse token,
        boolean isOnboarding
) {}