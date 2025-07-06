package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutineStepService {
    private final RoutineStepRepository routineStepRepository;
    private final RoutineRepository routineRepository;

    /**
     * 예외 : userId가 match되지 않고, 스텝의 사이즈가 초과되었을때
     */
    @Transactional
    // 특정 루틴에 스텝 추가
    public Object addStepToRoutine(UUID routineId, RoutineStepRequest request, User currentUser) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        if (!routine.getUser().getId().equals(currentUser.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        List<RoutineStep> existingSteps = routineStepRepository.findByRoutine(routine);
        // 이미 스텝이 6개 이상이면 예외
        if (existingSteps.size() >= 6) {
            throw new CustomException(ErrorCode.STEP_OVERLOADED);
        }

        RoutineStep newStep = RoutineStep.builder()
                .routine(routine)
                .name(request.getName())
                .stepOrder(request.getStepOrder())
                .estimatedTime(request.getEstimatedTime() != null ? LocalTime.parse(request.getEstimatedTime()) : null)
                .build();

        routineStepRepository.save(newStep);
        // response 응답용
        return Map.of("message", "스텝이 성공적으로 추가되었습니다.");
    }

}
