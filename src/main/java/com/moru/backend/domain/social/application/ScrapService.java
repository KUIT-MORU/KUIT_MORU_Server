package com.moru.backend.domain.social.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final RoutineRepository routineRepository;
    private final RoutineUserActionRepository routineUserActionRepository;

    public Long countScrap(UUID routineId) {
        return routineUserActionRepository.countByRoutineIdAndActionType(routineId, ActionType.SCRAP);
    }

    @Transactional
    public void scrap(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        boolean alreadyExists = routineUserActionRepository.existsByUserIdAndRoutineIdAndActionType(
                routineId, user.getId(), ActionType.SCRAP
        );

        if(alreadyExists) {
            throw new CustomException(ErrorCode.SCRAP_ALREADY_EXISTS);
        }

        RoutineUserAction routineUserAction = RoutineUserAction.builder()
                .user(user)
                .routine(routine)
                .actionType(ActionType.SCRAP)
                .build();

        routineUserActionRepository.save(routineUserAction);
    }

    @Transactional
    public void unscrap(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        RoutineUserAction action = routineUserActionRepository.findByUserIdAndRoutineIdAndActionType(user.getId(), routineId, ActionType.SCRAP)
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));

        routineUserActionRepository.delete(action);
    }
}
