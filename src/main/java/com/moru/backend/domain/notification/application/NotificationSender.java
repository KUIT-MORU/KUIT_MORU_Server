package com.moru.backend.domain.notification.application;

import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.global.fcm.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSender {
    private final UserRepository userRepository;
    private final FcmService fcmService;

    public void sendRoutineReminder(UUID receiverId, String title, String body) {
        userRepository.findById(receiverId).ifPresentOrElse(user -> {
            try {
                fcmService.sendMessage(user.getFcmToken(), title, body);
            } catch (Exception e) {
                log.warn("푸시 알림 전송 실패: userId={}, err={}", user.getId(), e.getMessage());
            }
        }, () -> log.warn("푸시 대상 유저 없음: receiverId={}", receiverId));
    }

}
