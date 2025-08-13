package com.moru.backend.global.validator;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
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

    public void validateCreateRequest(RoutineCreateRequest request) {

        // 간편 / 집중 루틴의 비즈니스 규칙을 검증
        if (request.isSimple()) {
            // 간편 루틴은 소요 시간을 가질 수 없음
            if (request.steps().stream().anyMatch(s -> s.estimatedTime() != null)) {
                throw new CustomException(ErrorCode.SIMPLE_ROUTINE_CANNOT_HAVE_TIME);
            }
        } else { // 집중 루틴
            // [ 집중 루틴은 *모든* 스텝에 소요 시간이 필수
            if (request.steps().stream().anyMatch(s -> s.estimatedTime() == null)) {
                throw new CustomException(ErrorCode.FOCUS_ROUTINE_REQUIRES_TIME_FOR_ALL_STEPS);
            }
        }
    }
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

}
