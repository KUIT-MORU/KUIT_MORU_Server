package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routines")
@RequiredArgsConstructor
@Tag(name = "루틴", description = "루틴 관련 API")
public class RoutineController {
    private final RoutineService routineService;
    private final UserRepository userRepository;

    @Operation(summary = "루틴 생성", description = "새로운 루틴을 생성합니다.")
    @PostMapping
    public Object createRoutine(
            @CurrentUser User currentUser,
            @Valid @RequestBody RoutineCreateRequest request) {
        return routineService.createRoutine(request, currentUser);
    }

    @Operation(summary = "내 루틴 목록 조회", description = "현재 로그인된 사용자의 루틴 목록을 조회합니다.")
    @GetMapping
    public List<RoutineListResponse> getRoutineList(
            @CurrentUser User currentUser
    ) {
        return routineService.getRoutineList(currentUser);
    }

    @Operation(summary = "루틴 상세 조회", description = "특정 루틴의 상세 정보를 조회합니다.")
    @GetMapping("/{routineId}")
    public RoutineDetailResponse getRoutineDetail(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId) {
        return routineService.getRoutineDetail(routineId, currentUser);
    }
} 