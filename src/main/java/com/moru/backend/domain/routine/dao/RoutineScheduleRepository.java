package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RoutineScheduleRepository extends JpaRepository<RoutineSchedule, UUID> {
    boolean existsByRoutineAndDayOfWeekAndTime(Routine routine, DayOfWeek dayOfWeek, LocalTime time);

    List<RoutineSchedule> findAllByRoutineId(UUID id);

    @Query("""
        SELECT rs FROM RoutineSchedule rs
        JOIN FETCH rs.routine r
        WHERE rs.alarmEnabled = true
        AND rs.dayOfWeek = :today
        AND r.user IS NOT NULL
    """)
    List<RoutineSchedule> findTodayEnabledSchedules(@Param("today") DayOfWeek today);

}
