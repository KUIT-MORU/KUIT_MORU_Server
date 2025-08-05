package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineScheduleHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineScheduleHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutineScheduleHistoryService {

    private final RoutineScheduleHistoryRepository RoutineScheduleHistoryRepository;
    private final RoutineScheduleHistoryRepository routineScheduleHistoryRepository;

    @Transactional
    public void recordNewHistory(Routine routine, List<DayOfWeek> days, LocalDateTime effectiveStartDateTime) {
        RoutineScheduleHistory newHistory = RoutineScheduleHistory.builder()
                .routine(routine)
                .scheduledDays(days)
                .effectiveStartDateTime(effectiveStartDateTime)
                .build();
        RoutineScheduleHistoryRepository.save(newHistory);
    }

    @Transactional
    public void endCurrentHistory(Routine routine, LocalDateTime effectiveEndDateTime) {
        List<RoutineScheduleHistory> currentHistories = routineScheduleHistoryRepository.findAllCurrentByRoutineId(routine.getId());
        currentHistories.forEach(history -> history.endHistory(effectiveEndDateTime));
    }

}
