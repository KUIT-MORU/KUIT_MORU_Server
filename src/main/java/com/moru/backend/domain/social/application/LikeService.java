package com.moru.backend.domain.social.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final RoutineRepository routineRepository;
    private final RoutineUserActionRepository routineUserActionRepository;

    public Long countLikes(UUID routineId) {
        // 루틴 아이디가 유효(존재, 소유주 맞음)하다는 전제하에 이루어짐.
        return routineUserActionRepository.countByRoutineIdAndActionType(routineId, ActionType.LIKE);
    }

    @Transactional
    public void like(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        boolean alreadyExists = routineUserActionRepository.existsByUserIdAndRoutineIdAndActionType(
                user.getId(), routineId, ActionType.LIKE
        );

        if(alreadyExists) {
            throw new CustomException(ErrorCode.LIKE_ALREADY_EXISTS);
        }

        RoutineUserAction routineUserAction = RoutineUserAction.builder()
                .user(user)
                .routine(routine)
                .actionType(ActionType.LIKE)
                .build();

        routineUserActionRepository.save(routineUserAction);
    }

    @Transactional
    public void unlike(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        RoutineUserAction action = routineUserActionRepository.findByUserIdAndRoutineIdAndActionType(user.getId(), routineId, ActionType.LIKE)
                .orElseThrow(() -> new CustomException(ErrorCode.LIKE_NOT_FOUND));

        routineUserActionRepository.delete(action);
    }
}
