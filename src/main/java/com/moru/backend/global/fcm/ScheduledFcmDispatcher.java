package com.moru.backend.global.fcm;

import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledFcmDispatcher {
    private static final int DEFAULT_RETRY_DELAY_SECONDS = 60;

    private final RedisQueueManager redisQueueManager;
    private final FcmService fcmService;

    // 매분 실행한다.
    @Scheduled(cron = "0 * * * * *")
    public void dispatchFcmMessages() {
        Set<ZSetOperations.TypedTuple<String>> messages =
                redisQueueManager.getDueMessages();

        for(ZSetOperations.TypedTuple<String> tuple: messages) {
            String rawJson = tuple.getValue();
            ScheduledFcmMessage message = redisQueueManager.deserialize(rawJson);
            boolean success = fcmService.sendMessage(message);

            if(!success) {
                redisQueueManager.enqueueRetry(message, DEFAULT_RETRY_DELAY_SECONDS);
            }

            redisQueueManager.removeMessage(rawJson);
        }
    }

}
