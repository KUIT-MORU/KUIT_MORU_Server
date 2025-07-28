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
public class RetryFcmJob {
    private final RedisQueueManager redisQueueManager;
    private final FcmService fcmService;

    // 1분마다 실패 메시지 재시도
    @Scheduled(cron = "0 * * * * *")
    public void retryFailedMessages() {
        Set<ZSetOperations.TypedTuple<String>> retryMessages = redisQueueManager.getRetryMessages();

        for (ZSetOperations.TypedTuple<String> tuple : retryMessages) {
            String rawJson = tuple.getValue();
            ScheduledFcmMessage message = redisQueueManager.deserialize(rawJson);
            boolean success = fcmService.sendMessage(message);

            if (success) {
                redisQueueManager.removeRetryMessage(rawJson);
                log.info("✅ FCM 재전송 성공: {}", message.getFcmToken());
            } else {
                log.warn("⚠️ FCM 재전송 실패 유지: {}", message.getFcmToken());
            }

        }

//        log.info("🔁 재전송 작업 완료: 총 {}건 시도", retryMessages.size());
    }
}
