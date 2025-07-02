package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineStepRepository extends JpaRepository<RoutineStep, UUID> {

    List<RoutineStep> findByRoutine(Routine routine);
} 