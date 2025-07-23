package com.moru.backend.domain.notification.application;

import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.user.application.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserService userService;

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


}
