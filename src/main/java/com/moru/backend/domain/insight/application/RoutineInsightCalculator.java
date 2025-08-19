package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.domain.TimeSlot;
import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineScheduleHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.RoutineScheduleHistory;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineInsightCalculator {
    private final RoutineLogRepository routineLogRepository;
    private final RoutineRepository routineRepository;
    private final RoutineScheduleHistoryRepository routineScheduleHistoryRepository;

    /**
     * 실천률 계산
     * @param user
     * @param startDate
     * @param endDate
     * @return
     */
    public Double calculateCompletionRate(User user, LocalDate startDate, LocalDate endDate) {
        double totalScheduled = 0;
        double totalCompleted = 0;

        // 유저 전체 루틴 조회
        List<Routine> userRoutines = routineRepository.findAllByUserId(user.getId());

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // 완료된 루틴 로그의 originalRoutineId + 날짜 기준으로 그룹핑
        Map<LocalDate, Set<UUID>> completedRoutinesByDate = routineLogRepository
                .findCompletedByUserIdAndPeriodWithSnapshot(user.getId(), startDateTime, endDateTime)
                .stream()
                .collect(Collectors.groupingBy(
                        log -> log.getStartedAt().toLocalDate(),
                        Collectors.mapping(log -> log.getRoutineSnapshot().getOriginalRoutineId(), Collectors.toSet())
                ));

        // 날짜별 반복
        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime nowAtMidnight = date.atStartOfDay(); // 자정 == 00:00
            DayOfWeek day = DayOfWeek.fromJavaDay(date.getDayOfWeek());

            for(Routine routine : userRoutines) {
                // 해당 시점에서 유효한 히스토리를 조회한다.
                List<RoutineScheduleHistory> histories =
                        routineScheduleHistoryRepository.findValidHistoryByRoutineIdAndDate(routine.getId(), nowAtMidnight);

                // 해당 요일에 예정된 루틴인지 확인
                boolean scheduled = histories.stream()
                        .anyMatch(h -> h.getScheduledDays().contains(day));

                if(scheduled) {
                    totalScheduled += 1;
                    if(completedRoutinesByDate
                            .getOrDefault(date, Collections.emptySet())
                            .contains(routine.getId())) {
                        totalCompleted += 1;
                    }
                }
            }
        }

        return totalScheduled == 0 ? 0.0 : (totalCompleted / totalScheduled);
    }

    /**
     * 주중 / 주말 실천 평균 개수 계산
     * @param user
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<String, Double> calculateWeekdayWeekendAvg(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Map<LocalDate, List<RoutineLog>> logsByDate = routineLogRepository.findCompletedByUserIdAndPeriodWithSnapshot(user.getId(), startDateTime, endDateTime)
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

    /**
     * 시간대별 루틴 실천 수 계산
     * @param user
     * @param startDate
     * @param endDate
     * @return
     */
    public Map<TimeSlot, Integer> calculateCompletionCountByTimeSlot(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<RoutineLog> logs = routineLogRepository.findCompletedByUserIdAndPeriodWithSnapshot(user.getId(), startDateTime, endDateTime);
        Map<TimeSlot, Integer> timeSlotCount = new EnumMap<>(TimeSlot.class);

        for(RoutineLog log : logs) {
            TimeSlot slot = TimeSlot.from(LocalTime.from(log.getStartedAt()));
            timeSlotCount.put(slot, timeSlotCount.getOrDefault(slot, 0) + 1);
        }

        return timeSlotCount;
    }
}
