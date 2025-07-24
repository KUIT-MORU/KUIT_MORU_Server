package com.moru.backend.global.redis;

import com.moru.backend.global.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final RedisTemplate<String, String> redisTemplate;
    private static final Duration REFRESH_TOKEN_EXPIRE = Duration.ofDays(7); // 7일간 유효

    @Override
    public void save(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(RedisKeyUtil.refreshTokenKey(userId), refreshToken);
    }

    @Override
    public String get(String userId) {
        return redisTemplate.opsForValue().get(RedisKeyUtil.refreshTokenKey(userId));
    }

    @Override
    public void delete(String userId) {
        redisTemplate.delete(RedisKeyUtil.refreshTokenKey(userId));
    }
}



