package com.moru.backend.domain.notification.api;

import com.moru.backend.domain.notification.application.NotificationService;
import com.moru.backend.domain.notification.dto.NotificationCursor;
import com.moru.backend.domain.notification.dto.NotificationResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import com.moru.backend.global.common.dto.ScrollResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    // 알림 목록 조회 (최근순)
    @Operation(summary = "알림 목록 조회(최근순)")
    @GetMapping
    public ResponseEntity<ScrollResponse<NotificationResponse, NotificationCursor>> getNotifications(
            @CurrentUser User user,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime cursorCreatedAt,
            @RequestParam(required = false) UUID lastNotificationId,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(user, cursorCreatedAt, lastNotificationId, size));
    }

    // 알림 삭제 처리
    @Operation(summary = "알림 삭제 처리")
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> markAsRead(
            @PathVariable UUID notificationId,
            @CurrentUser User user
    ) {
        notificationService.delete(notificationId, user.getId());
        return ResponseEntity.noContent().build();
    }

    // 알림 수 조회
    @Operation(summary = "알림 수 조회")
    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@CurrentUser User user) {
        int count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(count);
    }
}
