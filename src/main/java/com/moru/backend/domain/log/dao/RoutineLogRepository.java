package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.RoutineLog;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RoutineLogRepository extends JpaRepository<RoutineLog, UUID> {
}
