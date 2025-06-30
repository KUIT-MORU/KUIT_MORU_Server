package com.moru.backend.domain.routine.dao;

import com.moru.backend.domain.routine.domain.RoutineApp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoutineAppRepository extends JpaRepository<RoutineApp, UUID> {
} 