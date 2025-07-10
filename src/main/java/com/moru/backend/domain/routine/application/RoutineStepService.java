package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import com.moru.backend.domain.routine.dto.response.RoutineStepDetailResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.RoutineStepValidator;
import com.moru.backend.global.validator.RoutineValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutineStepService {
    private final RoutineStepRepository routineStepRepository;
    private final RoutineRepository routineRepository;
    private final RoutineStepValidator routineStepValidator;
    private final RoutineValidator routineValidator;

    /**
     * 예외 : userId가 match되지 않고, 스텝의 사이즈가 초과되었을때
     * 검사 : 이미 해당 stepOrder에 다른 Step이 있으면, 로직 추가
     */
    @Transactional
    // 특정 루틴에 스텝 추가
    public Object addStepToRoutine(UUID routineId, RoutineStepRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        List<RoutineStep> existingSteps = routineStepValidator.validateStepCountLimit(routine);

        // stepOrder 검사 로직 추가
        int newStepOrder = request.stepOrder();

        if (newStepOrder > existingSteps.size()) {
            newStepOrder = existingSteps.size() + 1;
        }

        // 기존 스텝들 순서 조정
        for (RoutineStep step : existingSteps) {
            if (step.getStepOrder() >= newStepOrder) {
                step.updateStepOrder(step.getStepOrder() + 1);
                routineStepRepository.save(step);
            }
        }

        RoutineStep.RoutineStepBuilder builder = RoutineStep.builder()
                .routine(routine)
                .name(request.name())
                .stepOrder(request.stepOrder());
        
        if (request.estimatedTime() != null) {
            builder.estimatedTime(request.estimatedTime());
        }
        
        RoutineStep newStep = builder.build();

        routineStepRepository.save(newStep);
        // response 응답용
        return Map.of(
                "message", "스텝이 성공적으로 추가되었습니다.",
                "stepOrder", newStepOrder
        );
    }

    @Transactional
    public List<RoutineStepDetailResponse> getRoutineSteps(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        //stepOrder 순서대로 정렬된 스텝 목록 조회
        List<RoutineStep> steps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        return steps.stream()
                .map(RoutineStepDetailResponse::from)
                .toList();
    }

    /**
     * 예외 : 해당 stepId를 가지는 루틴이 존재하지 않을 때.
     */
    @Transactional
    public Object updateStep(UUID routineId, UUID stepId, RoutineStepRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        RoutineStep step = routineStepValidator.validateStepAndRoutineRelation(stepId, routineId);

        int oldStepOrder = step.getStepOrder();
        int newStepOrder = request.stepOrder();

        // 순서가 변경된 경우에만 순서 조정
        if (oldStepOrder != newStepOrder) {
            List<RoutineStep> allSteps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
            routineStepValidator.validateStepOrder(routine, newStepOrder);

            //기존 스텝들 순서 조정
            for (RoutineStep otherStep : allSteps) {
                if (otherStep.getId().equals(stepId)) {
                    continue; //현재 수정 중인 스텝 건너뜀
                }

                int currentOrder = otherStep.getStepOrder();

                if (oldStepOrder < newStepOrder) {
                    // 뒤로 이동하는 경우: oldStepOrder와 newStepOrder 사이의 스텝들을 앞으로 이동
                    if (currentOrder > oldStepOrder && currentOrder <= newStepOrder) {
                        otherStep.updateStepOrder(currentOrder - 1);
                        routineStepRepository.save(otherStep);
                    }
                } else {
                    // 앞으로 이동하는 경우: newStepOrder와 oldStepOrder 사이의 스텝들을 뒤로 이동
                    if (currentOrder >= newStepOrder && currentOrder < oldStepOrder) {
                        otherStep.updateStepOrder(currentOrder + 1);
                        routineStepRepository.save(otherStep);
                    }
                }
            }
        }

        // 스텝 정보 업데이트
        step.updateName(request.name());
        step.updateStepOrder(newStepOrder);
        if (request.estimatedTime() != null) {
            step.updateEstimatedTime(request.estimatedTime());
        }

        routineStepRepository.save(step);
        return Map.of("message", "스텝이 성공적으로 수정되었습니다.");
    }

    @Transactional
    public Object deleteStep(UUID routineId, UUID stepId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        RoutineStep step = routineStepValidator.validateStepAndRoutineRelation(stepId, routineId);

        int deletedStepOrder = step.getStepOrder();

        routineStepRepository.delete(step);

        //삭제된 스텝 이후의 스텝들의 순서를 한칸씩 앞으로 이동하기
        List<RoutineStep> remainingSteps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        for (RoutineStep remainingStep : remainingSteps) {
            if (remainingStep.getStepOrder() > deletedStepOrder) {
                remainingStep.updateStepOrder(remainingStep.getStepOrder() - 1);
                routineStepRepository.save(remainingStep);
            }
        }
        return Map.of("message", "스텝이 성공적으로 삭제되었습니다.");
    }
}
