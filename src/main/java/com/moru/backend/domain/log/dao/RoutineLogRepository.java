package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.RoutineLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, UUID> {
    @Query("""
        SELECT rl FROM RoutineLog rl
        JOIN FETCH rl.routineSnapshot snap
        LEFT JOIN FETCH snap.tagSnapshots
        LEFT JOIN FETCH snap.appSnapshots
        LEFT JOIN FETCH snap.stepSnapshots
        LEFT JOIN FETCH rl.routineStepLogs stepLog
        LEFT JOIN FETCH stepLog.routineStep
        WHERE rl.id = :logId
    """)
    Optional<RoutineLog> findByRoutineLogIdWithSnapshotAndSteps(UUID logId);

    @Query("""
        SELECT rl FROM RoutineLog rl
        JOIN FETCH rl.routineSnapshot snap
        LEFT JOIN FETCH snap.tagSnapshots
        WHERE rl.user.id = :userId
        ORDER BY rl.startedAt DESC
    """)
    List<RoutineLog> findAllByUserIdWithSnapshot(UUID userId);

    List<RoutineLog> findByUserIdAndStartedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end);


    @EntityGraph(attributePaths = {
            "routineSnapshot",
            "routineSnapshot.tagSnapshots"
    })
    Page<RoutineLog> findByUserIdAndIsSimpleFalse(UUID userId, Pageable pageable);

    @Query("""
        SELECT rl FROM RoutineLog rl
        JOIN FETCH rl.routineSnapshot rs
        WHERE rl.user.id = :userId AND DATE(rl.createdAt) = :date
    """)
    List<RoutineLog> findByUserIdAndDateWithSnapshot(@Param("userId") UUID userId, @Param("date") LocalDate date);

    // 실행 중인 루틴 로그들 조회하기기
    @Query(value = "SELECT DISTINCT user_id FROM routine_log WHERE ended_at IS NULL ORDER BY RAND() LIMIT :count", nativeQuery = true)
    List<UUID> findRandomActiveUserIds(@Param("count") int count);

    @Query("SELECT rl FROM RoutineLog rl WHERE rl.user.id = :userId AND rl.endedAt IS NULL")
    Optional<RoutineLog> findActiveByUserId(@Param("userId") UUID userId);
    
}
