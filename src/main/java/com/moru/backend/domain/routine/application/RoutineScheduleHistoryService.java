package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineScheduleHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineScheduleHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoutineScheduleHistoryService {

    private final RoutineScheduleHistoryRepository routineScheduleHistoryRepository;

    @Transactional
    public void recordNewHistory(Routine routine, List<DayOfWeek> days, LocalDateTime effectiveStartDateTime) {
        RoutineScheduleHistory newHistory = RoutineScheduleHistory.builder()
                .routine(routine)
                .scheduledDays(days)
                .effectiveStartDateTime(effectiveStartDateTime)
                .build();
        routineScheduleHistoryRepository.save(newHistory);
    }

    @Transactional
    public void endCurrentHistory(Routine routine, LocalDateTime effectiveEndDateTime) {
        List<RoutineScheduleHistory> currentHistories = routineScheduleHistoryRepository.findAllCurrentByRoutineId(routine.getId());

        if (currentHistories.size() > 1) {
            log.warn("비정상적인 데이터가 발견되었습니다. routineId: {} 에 대해 종료되지 않은 히스토리가 {}개 존재합니다. 모든 히스토리를 종료 처리합니다.",
                    routine.getId(), currentHistories.size());
        }

        currentHistories.forEach(history -> history.endHistory(effectiveEndDateTime));
    }

}
