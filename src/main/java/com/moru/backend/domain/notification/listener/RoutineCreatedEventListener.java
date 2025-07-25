package com.moru.backend.domain.notification.listener;

import com.moru.backend.domain.notification.application.NotificationService;
import com.moru.backend.domain.notification.event.RoutineCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class RoutineCreatedEventListener {
    private final NotificationService notificationService;

    @Async
    @EventListener
    public void handleRoutineCreated(RoutineCreatedEvent event) {
        try {
            notificationService.sendRoutineCreated(
                    event.getSenderId(),
                    event.getRoutineId(),
                    event.getCreatedAt()
            );
        } catch (Exception e) {
            log.warn("루틴 생성 알림 전송 실패: {}", e.getMessage());
        }
    }
}
