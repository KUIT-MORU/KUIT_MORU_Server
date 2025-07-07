package com.moru.backend.global.validator;

import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoutineStepValidator {
    private final RoutineStepRepository routineStepRepository;

    /**
     * 스텝 존재 여부와 루틴과의 관계를 검증.
     * @param stepId 스텝 ID
     * @param routineId 루틴 ID
     * @return 검증된 스텝 엔티티
     */
    public RoutineStep validateStepAndRoutineRelation(UUID stepId, UUID routineId) {
        RoutineStep step = routineStepRepository.findById(stepId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_STEP_NOT_FOUND));

        if(!step.getRoutine().getId().equals(routineId)) {
            throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
        }
        return step;
    }

    /**
     * 스텝 개수 제한을 검증.
     * @param routine 루틴 엔티티
     */
    public List<RoutineStep> validateStepCountLimit(Routine routine) {
        List<RoutineStep> existingSteps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        if (existingSteps.size() >= 6) {
            throw new CustomException(ErrorCode.STEP_OVERLOADED);
        }
        return existingSteps;
    }

    /**
     * 스텝 순서의 유효성을 검증
     * @param routine 루틴 엔티티
     * @param newStepOrder 새로운 스텝 순서
     */
    public void validateStepOrder(Routine routine, int newStepOrder) {
        List<RoutineStep> allSteps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        if (newStepOrder < 1 || newStepOrder > allSteps.size()) {
            throw new CustomException(ErrorCode.INVALID_STEP_ORDER);
        }
    }
}
