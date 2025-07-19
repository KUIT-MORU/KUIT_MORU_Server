package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.schedule.RoutineScheduleHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RoutineScheduleHistoryRepository extends JpaRepository<RoutineScheduleHistory, UUID> {
    // 특정 날짜에 유효한 히스토리
    @Query("""
        SELECT h FROM RoutineScheduleHistory h
        WHERE h.routine.id = :routineId
          AND h.effectiveStartDateTime <= :date
          AND (h.effectiveEndDateTime IS NULL OR h.effectiveEndDateTime >= :date)
    """)
    List<RoutineScheduleHistory> findValidHistoryByRoutineIdAndDate(UUID routineId, LocalDateTime dateTime);

    // 현재 유효한 히스토리 1개만 조회
    @Query("""
        SELECT h FROM RoutineScheduleHistory h
        WHERE h.routine.id = :routineId
          AND h.effectiveEndDateTime IS NULL
    """)
    RoutineScheduleHistory findCurrentByRoutineId(UUID routineId);
}
