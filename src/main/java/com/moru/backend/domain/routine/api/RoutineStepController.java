package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineStepService;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import com.moru.backend.domain.routine.dto.response.RoutineStepDetailResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routines/{routineId}/steps")
@RequiredArgsConstructor
@Tag(name = "루틴 스텝", description = "루팁 스텝 관련 API")
public class RoutineStepController {
    private final RoutineStepService routineStepService;

    // @Valid : RoutineStepRequest에 붙인 @NotNull, @Size의 제약 조건을 검사 -> MethodArgumentNotValidException 예외 발생
    @Operation(summary = "루틴에 스텝 추가", description = "특정 루틴에 새로운 스텝 추가")
    @PostMapping
    public Object addStepToRoutine(
            @PathVariable UUID routineId,
            @CurrentUser User currentUser,
            @Valid @RequestBody RoutineStepRequest request) {
        return routineStepService.addStepToRoutine(routineId, request, currentUser);
    }

    @Operation(summary = "루틴의 스텝 목록 조회", description = "특정 루틴의 모든 스텝을 조회")
    @GetMapping
    public List<RoutineStepDetailResponse> getRoutineSteps(
            @PathVariable UUID routineId,
            @CurrentUser User currentUser) {
        return routineStepService.getRoutineSteps(routineId, currentUser);
    }

    @Operation(summary = "특정 스텝 수정", description = "특정 루틴의 특정 스텝을 수정")
    @PatchMapping("/{stepId}")
    public Object updateStep(@PathVariable UUID routineId,
                             @PathVariable UUID stepId,
                             @CurrentUser User currentUser,
                             @Valid @RequestBody RoutineStepRequest request) {
        return routineStepService.updateStep(routineId, stepId, request, currentUser);
    }
}
