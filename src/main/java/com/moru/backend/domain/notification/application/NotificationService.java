package com.moru.backend.domain.notification.application;

import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.notification.dto.NotificationCursor;
import com.moru.backend.domain.notification.dto.NotificationResponse;
import com.moru.backend.domain.notification.mapper.NotificationMapper;
import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.common.dto.ScrollResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.fcm.FcmService;
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
    private final UserRepository userRepository;
    private final UserService userService;
    private final FcmService fcmService;
    private final RoutineService routineService;

    // 루틴 생성 알림
    public void sendRoutineCreated(UUID receiverId, UUID senderId, UUID routineId) {
        if(!routineService.isUserVisibleById(routineId)) {
            return;
        }

        // DB 저장
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .resourceId(routineId)
                .type(NotificationType.ROUTINE_CREATED)
                .build();

        notificationRepository.save(notification);
    }

    // 팔로우 알림
    public void sendFollowReceived(UUID receiverId, UUID senderId) {
        //DB 저장
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .type(NotificationType.FOLLOW_RECEIVED)
                .build();
        notificationRepository.save(notification);

        //FCM 발송
        userRepository.findById(receiverId).ifPresent(receiver -> {
            String fcmToken = receiver.getFcmToken();
            if(fcmToken != null && !fcmToken.isBlank()) {
                String receiverName = userService.getNicknameById(receiverId);
                String senderName = userService.getNicknameById(senderId);
                String body = senderName + "님이 회원님을 팔로우하기 시작했습니다.";
                fcmService.sendMessage(fcmToken, receiverName, body);
            }
        });
    }

    // 루틴 스케줄 알림
    public void sendRoutineReminder(UUID receiverId, UUID routineId) {
        // DB 저장
        Notification notification = Notification.builder()
                .receiverId(receiverId)
                .senderId(null) // 시스템 알림
                .resourceId(routineId)
                .type(NotificationType.ROUTINE_REMINDER)
                .build();
        notificationRepository.save(notification);

        //FCM 발송
        userRepository.findById(receiverId).ifPresent(receiver -> {
            String fcmToken = receiver.getFcmToken();
            if(fcmToken != null && !fcmToken.isBlank()) {
                String routineTitle = routineService.getRoutineTitleById(routineId);
                String title = "루틴 알림";
                String body = "지금 \"" + routineTitle + "\" 루틴을 실천해보세요!";
                fcmService.sendMessage(fcmToken, title, body);
            }
        });
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public int getUnreadCount(UUID receiverId) {
        return notificationRepository.countByReceiverId(receiverId);
    }

    @Transactional
    public void delete(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiverId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN_NOTIFICATION_ACCESS);
        }

        notificationRepository.delete(notification);
    }
}
