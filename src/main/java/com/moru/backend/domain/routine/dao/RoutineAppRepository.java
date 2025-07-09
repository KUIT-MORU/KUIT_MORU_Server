package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoutineAppRepository extends JpaRepository<RoutineApp, UUID> {

    List<RoutineApp> findByRoutine(Routine routine);
    boolean existsByRoutineAndApp_Id(Routine routine, UUID appId);
    Optional<RoutineApp> findByRoutineAndApp_Id(Routine routine, UUID appId);
} 