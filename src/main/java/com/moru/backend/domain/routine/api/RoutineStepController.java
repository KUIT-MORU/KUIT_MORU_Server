package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineStepService;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/routines/{routineId}/steps")
@RequiredArgsConstructor
@Tag(name = "루틴 스텝", description = "루팁 스텝 관련 API")
public class RoutineStepController {
    private final RoutineStepService routineStepService;
    private final UserRepository userRepository;

    // @Valid : RoutineStepRequest에 붙인 @NotNull, @Size의 제약 조건을 검사 -> MethodArgumentNotValidException 예외 발생
    @Operation(summary = "루틴에 스텝 추가", description = "특정 루틴에 새로운 스텝 추가")
    @PostMapping
    public Object addStepToRoutine(@PathVariable UUID routineId, @Valid @RequestBody RoutineStepRequest request) {
        User currentUser = getCurrentUser();
        return routineStepService.addStepToRoutine(routineId, request, currentUser);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            UUID userId = (UUID) principal;
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
}
