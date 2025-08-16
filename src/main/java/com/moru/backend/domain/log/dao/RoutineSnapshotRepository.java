package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoutineSnapshotRepository extends JpaRepository<RoutineSnapshot, Long> {
    // [추가] 스텝 스냅샷만
    @Query("""
        SELECT s FROM RoutineSnapshot s
        LEFT JOIN FETCH s.stepSnapshots
        WHERE s.id = :snapshotId
    """)
    Optional<RoutineSnapshot> fetchStepSnapshots(UUID snapshotId);

    // [추가] 앱 스냅샷만
    @Query("""
        SELECT s FROM RoutineSnapshot s
        LEFT JOIN FETCH s.appSnapshots
        WHERE s.id = :snapshotId
    """)
    Optional<RoutineSnapshot> fetchAppSnapshots(UUID snapshotId);

    // [추가] 태그 스냅샷만 (필요할 때만 호출)
    @Query("""
        SELECT s FROM RoutineSnapshot s
        LEFT JOIN FETCH s.tagSnapshots
        WHERE s.id = :snapshotId
    """)
    Optional<RoutineSnapshot> fetchTagSnapshots(UUID snapshotId);
}
