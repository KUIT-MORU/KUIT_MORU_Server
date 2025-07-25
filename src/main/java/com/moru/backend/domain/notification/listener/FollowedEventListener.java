package com.moru.backend.domain.notification.listener;

import com.moru.backend.domain.notification.application.NotificationService;
import com.moru.backend.domain.notification.event.FollowedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowedEventListener {

    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleFollowed(FollowedEvent event) {
        try {
            notificationService.sendFollowReceived(
                    event.getReceiverId(),
                    event.getSenderId(),
                    event.getCreatedAt()
            );
        } catch (Exception e) {
            log.warn("팔로우 알림 전송 실패: {}", e.getMessage());
        }
    }
}
