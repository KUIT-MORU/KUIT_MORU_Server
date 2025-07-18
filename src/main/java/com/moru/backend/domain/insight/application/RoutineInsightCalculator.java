package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.domain.TimeSlot;
import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
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

            // 해당 날짜에 실행되고 완료된 루틴 로그 조회
            Set<UUID> completedRoutineIds = routineLogRepository.findCompletedByUserIdAndDateWithSnapshot(user.getId(), date).stream()
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

    // 주중 / 주말 실천 평균 개수 계산
    public Map<String, Double> calculateWeekdayWeekendAvg(User user, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, List<RoutineLog>> logsByDate = routineLogRepository.findCompletedByUserIdAndPeriodWithSnapshot(user.getId(), startDate, endDate)
                .stream()
                .collect(Collectors.groupingBy(log -> log.getStartedAt().toLocalDate()));

        int weekdayCount = 0;
        int weekendCount = 0;
        int weekdayDayCount = 0;
        int weekendDayCount = 0;

        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            int count = logsByDate.getOrDefault(date, Collections.emptyList()).size();
            switch(date.getDayOfWeek()) {
                case SATURDAY, SUNDAY -> {
                    weekendCount += count;
                    weekendDayCount += 1;
                }
                default -> {
                    weekdayCount += count;
                    weekdayDayCount += 1;
                }
            }
        }

        double weekdayAvg = weekdayDayCount == 0 ? 0.0 : ((double) weekdayCount / weekdayDayCount);
        double weekendAvg = weekendDayCount == 0 ? 0.0 : ((double) weekendCount / weekendDayCount);

        return Map.of(
                "weekday", weekdayAvg,
                "weekend", weekendAvg
        );
    }

    // 시간대별 루틴 실천 수 계산
    public Map<TimeSlot, Integer> calculateCompletionCountByTimeSlot(User user, LocalDate startDate, LocalDate endDate) {
        List<RoutineLog> logs = routineLogRepository.findCompletedByUserIdAndPeriodWithSnapshot(user.getId(), startDate, endDate);
        Map<TimeSlot, Integer> timeSlotCount = new EnumMap<>(TimeSlot.class);

        for(RoutineLog log : logs) {
            TimeSlot slot = TimeSlot.from(LocalTime.from(log.getStartedAt()));
            timeSlotCount.put(slot, timeSlotCount.getOrDefault(slot, 0) + 1);
        }

        return timeSlotCount;
    }
}
