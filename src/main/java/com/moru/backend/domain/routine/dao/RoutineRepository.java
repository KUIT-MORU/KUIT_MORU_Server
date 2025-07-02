package com.moru.backend.domain.routine.dao;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;

public interface RoutineRepository extends JpaRepository<Routine, UUID> {

    void findAllByUser(User user);
    
}
