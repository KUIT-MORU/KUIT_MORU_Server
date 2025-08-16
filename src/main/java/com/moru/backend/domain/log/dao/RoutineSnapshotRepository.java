package com.moru.backend.domain.log.dao;

import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineSnapshotRepository extends JpaRepository<RoutineSnapshot, Long> {
}
