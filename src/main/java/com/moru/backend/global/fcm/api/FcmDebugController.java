package com.moru.backend.global.fcm.api;

import com.moru.backend.global.fcm.RedisQueueManager;
import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/fcm/debug")
@RequiredArgsConstructor
//@PreAuthorize("hasRole('ADMIN')")

public class FcmDebugController {
    private final RedisQueueManager redisQueueManager;

    @GetMapping("/scheduled")
    public ResponseEntity<List<ScheduledFcmMessage>> getAllScheduledMessages() {
        Set<ZSetOperations.TypedTuple<String>> entries = redisQueueManager.getAllScheduledMessages();

        List<ScheduledFcmMessage> messages = entries.stream()
                .map(ZSetOperations.TypedTuple::getValue)
                .map(redisQueueManager::deserialize)
                .toList();

        return ResponseEntity.ok(messages);
    }
}
