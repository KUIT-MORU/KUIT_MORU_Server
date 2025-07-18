package com.moru.backend.domain.global.redis;

import com.moru.backend.global.util.RedisKeyUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import java.util.concurrent.TimeUnit;

@Repository
public class TokenBlacklistRepositoryImpl implements TokenBlacklistRepository {
    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistRepositoryImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addBlacklist(String token, long expirationMillis) {
        String key = RedisKeyUtil.blacklistKey(token);
        redisTemplate.opsForValue().set(key, "", expirationMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        String key = RedisKeyUtil.blacklistKey(token);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
} 