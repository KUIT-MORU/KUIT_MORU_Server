package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.RoutineLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

    boolean existsByUserIdAndRoutineSnapshot_RoutineIdAndStartedAtIsNotNullAndEndedAtIsNull(UUID userId, UUID routineId);

    @EntityGraph(attributePaths = {
            "routineSnapshot",
            "routineSnapshot.tagSnapshots"
    })
    Page<RoutineLog> findByUserId(UUID userId, Pageable pageable);
}
