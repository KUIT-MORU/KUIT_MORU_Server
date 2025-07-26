package com.moru.backend.global.fcm;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
/**
* 새로 생성한 스케줄 추가
* 오늘 요일에 해당하고, 아직 시간이 지나지 않은 경우만 등록한다.
*/
public class RoutineScheduleFcmPreloader {
    private final RedisQueueManager redisQueueManager;

    public void preloadRoutineScheduleFcm(Routine routine) {
        User user = routine.getUser();
        String fcmToken = user.getFcmToken();

        if (fcmToken == null || fcmToken.isBlank()) return;

        List<RoutineSchedule> schedules = routine.getRoutineSchedules();

        LocalDate today = LocalDate.now();
        DayOfWeek todayDayOfWeek = DayOfWeek.fromJavaDay(today.getDayOfWeek());
        LocalTime nowTime = LocalTime.now();


        for(RoutineSchedule schedule : schedules) {
            if(!schedule.isAlarmEnabled()) { continue; }

            // 오늘 요일에 해당하고, 아직 시간이 지나지 않은 경우만
            if(schedule.getDayOfWeek() != todayDayOfWeek) { continue; }
            if(!schedule.getTime().isAfter(nowTime)) { continue; }

            LocalDateTime scheduledTime = today.atTime(schedule.getTime());

            ScheduledFcmMessage message = ScheduledFcmMessage.builder()
                    .receiverId(user.getId())
                    .nickname(user.getNickname())
                    .routineId(routine.getId())
                    .routineTitle(routine.getTitle())
                    .fcmToken(user.getFcmToken())
                    .scheduledTime(scheduledTime)
                    .retryCount(0)
                    .build();

            redisQueueManager.enqueueScheduled(message);
        }

    }

    public void refreshRoutineScheduleFcm(Routine routine) {
        // 해당 루틴에 대해 설정된 기존 알림을 모두 삭제
        redisQueueManager.removeScheduledMessagesByRoutineId(routine.getId());

        // 현재 설정된 스케줄 기준으로 다시 등록
        preloadRoutineScheduleFcm(routine);
    }

    public void removeRoutineScheduleFcm(Routine routine) {
        redisQueueManager.removeScheduledMessagesByRoutineId(routine.getId());
    }
}
