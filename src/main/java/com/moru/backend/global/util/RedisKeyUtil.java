package com.moru.backend.global.util;

import java.util.UUID;

public class RedisKeyUtil {
    public static String routineViewKey(UUID routineId, UUID userId) {
        return "routine:view:" + routineId + ":user:" + userId;
    }

    public static String refreshTokenKey(String userId) {
        return "refresh:" + userId;
    }

    public static String blacklistKey(String token) {
        return "blacklist:" + token;
    }
} 