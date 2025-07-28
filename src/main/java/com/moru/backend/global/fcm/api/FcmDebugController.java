package com.moru.backend.global.fcm.api;

import com.moru.backend.global.fcm.RedisQueueManager;
import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/admin/fcm")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")

public class FcmDebugController {
    private final RedisQueueManager redisQueueManager;

    @Operation(summary = "FCM 루틴 큐 목록 조회")
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
