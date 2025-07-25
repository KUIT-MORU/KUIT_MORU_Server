package com.moru.backend.global.fcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import com.moru.backend.global.util.RedisKeyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisQueueManager {
    private static final String ROUTINE_SCHEDULE_QUEUE_KEY = RedisKeyUtil.ROUTINE_SCHEDULE_QUEUE;
    private static final String RETRY_QUEUE_KEY = RedisKeyUtil.RETRY_QUEUE;

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis ZSET에 메시지 추가 (전송 예정 시간 기준)
     */
    public void enqueueScheduled(ScheduledFcmMessage message) {
        try {
            String json = objectMapper.writeValueAsString(message);
            long score = message.getScheduledTime().toEpochSecond(ZoneOffset.UTC);
            redisTemplate.opsForZSet().add(ROUTINE_SCHEDULE_QUEUE_KEY, json, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("FCM 메시지 직렬화 실패", e);
        }
    }

    /**
     * 현재 시각까지 도달한 메시지 가져오기!
     */
    public Set<ZSetOperations.TypedTuple<String>> getDueMessages() {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return redisTemplate.opsForZSet().rangeByScoreWithScores(ROUTINE_SCHEDULE_QUEUE_KEY, 0, now);
    }

    /**
     * 메시지 삭제
     */
    public void removeMessage(String json) {
        redisTemplate.opsForZSet().remove(ROUTINE_SCHEDULE_QUEUE_KEY, json);
    }

    /**
     * 재시도 큐에 추가 (스코어: 현재 시간 + delay)
     */
    public void enqueueRetry(ScheduledFcmMessage message, int delaySeconds) {
        try {
            String json = objectMapper.writeValueAsString(message);
            long score = LocalDateTime.now().plusSeconds(delaySeconds).toEpochSecond(ZoneOffset.UTC);
            redisTemplate.opsForZSet().add(RETRY_QUEUE_KEY, json, score);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("FCM 재시도 메시지 직렬화 실패", e);
        }
    }

    /**
     * 재시도 대상 메시지 조회
     */
    public Set<ZSetOperations.TypedTuple<String>> getRetryMessages() {
        long now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return redisTemplate.opsForZSet().rangeByScoreWithScores(RETRY_QUEUE_KEY, 0, now);
    }

    /**
     * 재시도 큐에서 메시지 제거
     */
    public void removeRetryMessage(String json) {
        redisTemplate.opsForZSet().remove(RETRY_QUEUE_KEY, json);
    }

    /**
     * JSON 역직렬화
     */
    public ScheduledFcmMessage deserialize(String json) {
        try {
            return objectMapper.readValue(json, ScheduledFcmMessage.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("FCM 메시지 역직렬화 실패", e);
        }
    }
}
