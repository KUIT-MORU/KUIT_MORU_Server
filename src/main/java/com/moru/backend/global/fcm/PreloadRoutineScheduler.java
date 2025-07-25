package com.moru.backend.global.fcm;

import com.moru.backend.domain.routine.dao.RoutineScheduleRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.global.fcm.dto.ScheduledFcmMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PreloadRoutineScheduler {
    private final RoutineScheduleRepository routineScheduleRepository;
    private final RedisQueueManager redisQueueManager;

    // 매일 자정에 실행한다.
    @Scheduled(cron = "0 0 0 * * *")
    public void preloadTodayRoutinePushMessage() {
        LocalDate today = LocalDate.now();
        DayOfWeek todyaDayOfWeek = DayOfWeek.fromJavaDay(today.getDayOfWeek());

        List<RoutineSchedule> schedules =
                routineScheduleRepository.findTodayEnabledSchedules(todyaDayOfWeek);

        log.info("🔄 Preloading FCM routine alarms for {}", today);

        for(RoutineSchedule schedule : schedules) {
            Routine routine = schedule.getRoutine();

            String fcmToken = routine.getUser().getFcmToken();
            if(fcmToken == null) { continue; }

            ScheduledFcmMessage message = ScheduledFcmMessage.builder()
                    .receiverId(routine.getUser().getId())
                    .nickname(routine.getUser().getNickname())
                    .routineTitle(routine.getTitle())
                    .fcmToken(fcmToken)
                    .scheduledTime(LocalDateTime.of(today, schedule.getTime()))
                    .retryCount(0)
                    .build();

            redisQueueManager.enqueueScheduled(message);
        }

        log.info("✅ Preload FCM routine alarms completed: {} messages queued", schedules.size());
    }
}
