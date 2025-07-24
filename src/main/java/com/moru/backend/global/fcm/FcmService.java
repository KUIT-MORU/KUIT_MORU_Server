package com.moru.backend.global.fcm;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {
    /**
     * 단일 디바이스에게 푸시 알림을 전송한다.
     */
    public void sendMessage(String fcmToken, String title, String body) {
        if(fcmToken == null || fcmToken.isBlank()) {
            log.warn("FCM 전송 실패: 유효하지 않은 토큰");
            return;
        }

        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 발송 성공: {}", response);
        } catch (Exception e) {
            log.error("❌ FCM 발송 실패: {}", e.getMessage(), e);
        }
    }
}
