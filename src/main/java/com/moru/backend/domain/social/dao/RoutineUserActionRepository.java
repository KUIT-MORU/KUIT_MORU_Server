package com.moru.backend.domain.social.dao;

import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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

    List<RoutineUserAction> findAllByUserIdAndActionType(UUID userId, ActionType actionType);

    @Query("""
        SELECT rua FROM RoutineUserAction rua
        JOIN FETCH rua.routine r
        WHERE rua.user.id = :userId
        AND rua.actionType = :actionType
        AND (
            :lastScrapId IS NULL OR
            (rua.createdAt < :lastCreatedAt) OR
            (rua.createdAt = :lastCreatedAt AND rua.id < :lastScrapId)
        )
        ORDER BY rua.createdAt DESC, rua.id DESC
    """)
    List<RoutineUserAction> findScrapsByCursor(
            @Param("userId") UUID userId,
            @Param("actionType") ActionType actionType,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastScrapId") UUID lastScrapId,
            Pageable pageable
    );

    @Query("""
        select rua.user.id as userId, rua.routine.id as routineId
        from RoutineUserAction rua
        where rua.actionType = :type
          and rua.user.id in :userIds
          and rua.routine.id in :routineIds
    """)
    List<Object[]> findExistingPairs(@Param("type") ActionType type,
                                     @Param("userIds") Collection<UUID> userIds,
                                     @Param("routineIds") Collection<UUID> routineIds);
}
