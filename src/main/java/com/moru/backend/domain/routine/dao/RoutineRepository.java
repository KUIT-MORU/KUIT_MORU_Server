package com.moru.backend.domain.routine.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moru.backend.domain.routine.domain.Routine;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {
    
}
