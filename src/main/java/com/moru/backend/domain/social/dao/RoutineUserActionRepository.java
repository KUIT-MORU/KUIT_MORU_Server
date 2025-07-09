package com.moru.backend.domain.social.dao;

import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoutineUserActionRepository extends JpaRepository<RoutineUserAction, UUID> {
    Optional<RoutineUserAction> findByUserIdAndRoutineIdAndActionType(
            UUID userId, UUID routineId, ActionType actionType
    );
    boolean existsByUserIdAndRoutineIdAndActionType(
            UUID userId, UUID routineId, ActionType actionType
    );
    Long countByRoutineIdAndActionType(UUID routineId, ActionType actionType);
}
