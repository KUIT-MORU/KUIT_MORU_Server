package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineTagRepository extends JpaRepository<RoutineTag, UUID> {

    List<RoutineTag> findByRoutine(Routine routine);
} 