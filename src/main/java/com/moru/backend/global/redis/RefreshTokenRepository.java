package com.moru.backend.global.redis;

public interface RefreshTokenRepository {
    void save(String userId, String refreshToken);
    String get(String userId);
    void delete(String userId);
}
