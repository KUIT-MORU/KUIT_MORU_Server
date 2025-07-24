package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.RoutineStepLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface RoutineStepLogRepository extends JpaRepository<RoutineStepLog, UUID> {
    Long countByRoutineLogIdAndIsCompletedFalse(UUID routineLogId);
}
