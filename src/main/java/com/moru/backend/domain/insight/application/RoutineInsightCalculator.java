package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineInsightCalculator {
    private final RoutineLogRepository routineLogRepository;
    private final RoutineRepository routineRepository;

    // 실천률 계산
    public Double calculateCompletionRate(User user, LocalDate startDate, LocalDate endDate) {
        double totalScheduled = 0;
        double totalCompleted = 0;

        List<Routine> userRoutines = routineRepository.findAllByUserId(user.getId());

        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek dayOfWeek = DayOfWeek.fromJavaDay(date.getDayOfWeek());

            // 해당 날짜에 실행된 루틴 로그 조회
            Set<UUID> completedRoutineIds = routineLogRepository.findByUserIdAndDateWithSnapshot(user.getId(), date).stream()
                    .map(log -> log.getRoutineSnapshot().getOriginalRoutineId())
                    .collect(Collectors.toSet());

            // 해당 날짜 시점에 유효했던 스케줄을 가진 루틴 필터링
            LocalDate currentDate = date;
            List<Routine> scheduledRoutines = userRoutines.stream()
                    .filter(routine -> hasScheduleOnDayBefore(routine, dayOfWeek, currentDate))
                    .toList();


            totalScheduled += scheduledRoutines.size();
            for(Routine routine : scheduledRoutines) {
                if(completedRoutineIds.contains(routine.getId())) {
                    totalCompleted += 1;
                }
            }
        }

        return totalScheduled == 0 ? 0.0 : (totalCompleted / totalScheduled);
    }

    // 해당 요일에 실행되도록 설정한 스케줄이 존재하고, 그 스케줄이 해당 날짜 이전에 수정되었는지 확인
    private boolean hasScheduleOnDayBefore(Routine routine, DayOfWeek targetDay, LocalDate date) {
        return routine.getRoutineSchedules().stream()
                .anyMatch(schedule ->
                    schedule.getDayOfWeek().equals(targetDay) &&
                    schedule.getUpdatedAt().toLocalDate().isBefore(date.plusDays(1))
                );
    }
}
