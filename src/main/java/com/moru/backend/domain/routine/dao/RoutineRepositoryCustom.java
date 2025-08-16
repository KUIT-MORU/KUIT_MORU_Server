package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface RoutineRepositoryCustom {
    Page<Routine> findRoutinesOrderByUpcoming(UUID userId, Pageable pageable);
}