package com.moru.backend.domain.notification.application;

import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.notification.dto.NotificationCursor;
import com.moru.backend.domain.notification.dto.NotificationResponse;
import com.moru.backend.domain.notification.mapper.NotificationMapper;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.common.dto.ScrollResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    // 루틴 생성 알림
    public void sendRoutineCreated(UUID receiverId, UUID senderId, UUID routineId) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .resourceId(routineId)
                .link("/routines/" + routineId)
                .type(NotificationType.ROUTINE_CREATED)
                .build();

        notificationRepository.save(notification);
    }

    // 팔로우 알림
    public void sendFollowReceived(UUID receiverId, UUID senderId) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .link("/user/" + senderId)
                .type(NotificationType.FOLLOW_RECEIVED)
                .build();
        notificationRepository.save(notification);
    }

    // 루틴 스케줄 알림
    public void sendRoutineReminder(UUID receiverId, UUID routineId) {
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(null) // 시스템 알림
                .resourceId(routineId)
                .link("/routines/" + routineId)
                .type(NotificationType.ROUTINE_REMINDER)
                .build();
        notificationRepository.save(notification);
    }

    // 알림 목록 조회
    public ScrollResponse<NotificationResponse, NotificationCursor> getNotifications(
            User user,
            LocalDateTime lastCreatedAt, UUID lastNotificationId, int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Notification> notifications = notificationRepository.findNotificationByCursor(
                user.getId(),
                lastCreatedAt,
                lastNotificationId,
                pageable
        );

        List<NotificationResponse> result = notifications.stream()
                .map(notificationMapper::toResponse)
                .toList();

        boolean hasNext = result.size() == limit;
        NotificationCursor nextCursor = hasNext
                ? new NotificationCursor(notifications.getLast().getCreatedAt(), notifications.getLast().getId())
                : null;
        return ScrollResponse.of(result, hasNext, nextCursor);
    }

    // 알림 읽음 처리
    @Transactional
    public void markAsRead(UUID notificationId, UUID receiverId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if(!notification.getReceiverId().equals(receiverId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_NOTIFICATION_ACCESS);
        }

        if(!notification.isRead()) {
            notification.markAsRead();
        }
    }

    @Transactional
    public void markAllAsRead(UUID receiverId) {
        notificationRepository.markAllAsRead(receiverId);
    }

    @Transactional
    public int getUnreadCount(UUID receiverId) {
        return notificationRepository.countByReceiverIdAndIsReadFalse(receiverId);
    }
}
