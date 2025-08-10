package com.moru.backend.domain.notification.mapper;

import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.notification.dto.NotificationResponse;
import com.moru.backend.domain.routine.application.RoutineQueryService;
import com.moru.backend.domain.user.application.UserService;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class NotificationMapper {
    private final UserService userService;
    private final S3Service s3Service;
    private final RoutineQueryService routineQueryService;

    public NotificationResponse toResponse(Notification notification) {
        String senderNickname = userService.getNicknameById(notification.getSenderId());

        String profileImageUrl = userService.getProfileImageUrlById(notification.getSenderId());

        String routineTitle = notification.getResourceId() != null
                ? routineQueryService.getRoutineTitleById(notification.getResourceId())
                : null;

        String message = switch(notification.getType()) {
            case NotificationType.ROUTINE_CREATED -> senderNickname + "님이 " + routineTitle + "을 생성했습니다.";
            case NotificationType.FOLLOW_RECEIVED -> {
                String receiverName = userService.getNicknameById(notification.getReceiverId());
                yield senderNickname + "님이 " + receiverName + "님을 팔로우했습니다.";
            }
        };

        return new NotificationResponse(
                notification.getId(),
                notification.getSenderId(),
                senderNickname,
                s3Service.getImageUrl(profileImageUrl),
                message,
                formatRelativeTime(notification.getCreatedAt())
        );
    }

    private String formatRelativeTime(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        if(duration.toMinutes() < 1) { return "방금 전"; }
        if(duration.toMinutes() < 60) { return duration.toMinutes() + "분 전"; }
        if(duration.toHours() < 24) { return duration.toHours() + "시간 전"; }
        return createdAt.toLocalDate().toString();
    }
}
