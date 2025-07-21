package com.moru.backend.global.validator;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoutineValidator {
    private final RoutineRepository routineRepository;

    /**
     * 루틴 존재 여부와 사용자 권한을 검증
     * @param routineId 루틴 ID
     * @param currentUser 현재 사용자
     * @return 검증된 루틴 엔티티
     */
    public Routine validateRoutineAndUserPermission(UUID routineId, User currentUser) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        if (!routine.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return routine;
    }

    /**
     * 공개 루틴이면 누구나, 비공개 루틴이면 작성자만 볼 수 있도록 권한 검증
     */
    public Routine validateRoutineViewPermission(UUID routineId, User currentUser) {
        Routine routine = routineRepository.findById(routineId)
            .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));
        if (routine.isUserVisible()) {
            return routine;
        }
        if (!routine.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }
        return routine;
    }
}
