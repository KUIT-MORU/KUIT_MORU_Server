package com.moru.backend.global.redis;

public interface TokenBlacklistRepository {
    void addBlacklist(String token, long expirationMillis);
    boolean isBlacklisted(String token);
} 