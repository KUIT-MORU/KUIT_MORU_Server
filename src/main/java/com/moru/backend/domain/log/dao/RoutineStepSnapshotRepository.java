package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoutineStepSnapshotRepository extends JpaRepository<RoutineStepSnapshot, UUID> {
    Optional<RoutineStepSnapshot> findByRoutineSnapshotIdAndStepOrder(UUID routineSnapshotId, Integer stepOrder);
}
