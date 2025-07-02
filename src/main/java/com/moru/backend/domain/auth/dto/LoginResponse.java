package com.moru.backend.domain.auth.dto;

public record LoginResponse (
        String accessToken,
        String refreshToken
) {}
