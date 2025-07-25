package com.moru.backend.global.fcm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Redis에 저장될 푸시 메시지 데이터 구조
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledFcmMessage implements Serializable {
    private UUID receiverId;          // 수신자 ID
    private String nickname;          // 푸시 헤드 메시지 ("{닉네임}님!")
    private String routineTitle;      // 푸시 바디 메시지 ("{루틴명}, 지금 할 시간이에요.")
    private String fcmToken;          // FCM 토큰
    private LocalDateTime scheduledTime; // 전송 예정 시각
    private int retryCount;           // 재시도 횟수 (기본값 0)
}
