package com.moru.backend.domain.notification.application;

import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.notification.dto.NotificationCursor;
import com.moru.backend.domain.notification.dto.NotificationResponse;
import com.moru.backend.domain.notification.mapper.NotificationMapper;
import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.social.application.FollowService;
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
    private final FollowService followService;
    private final NotificationSender notificationSender;

    // 루틴 생성 알림
    public void sendRoutineCreated(UUID senderId, UUID routineId) {
        // 루틴의 유저 공개 여부 확인
        if(!routineService.isUserVisibleById(routineId)) {
            return;
        }

        // 발송자 팔로워 목록 조회
        List<UUID> followerIds = followService.findFollowerIdsByUserId(senderId);

        // 알림 생성
        for(UUID followerId : followerIds) {
            Notification notification = Notification.builder()
                    .receiverId(followerId)
                    .senderId(senderId)
                    .resourceId(routineId)
                    .type(NotificationType.ROUTINE_CREATED)
                    .build();

            notificationRepository.save(notification);
        }
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
    }

    // 루틴 스케줄 알림
    public void sendRoutineReminder(UUID receiverId, UUID routineId) {
        //FCM 발송
        String routineTitle = routineService.getRoutineTitleById(routineId);
        String nickname = userService.getNicknameById(receiverId);

        String title = nickname + "님!";
        String body = "\"" + routineTitle + "\", 지금 할 시간이에요.";

        notificationSender.sendRoutineReminder(receiverId, title, body);
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
