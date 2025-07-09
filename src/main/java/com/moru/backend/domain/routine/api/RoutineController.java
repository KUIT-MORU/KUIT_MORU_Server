package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Object> createRoutine(
            @CurrentUser User currentUser,
            @Valid @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.ok(routineService.createRoutine(request, currentUser));
    }

    @Operation(summary = "내 루틴 목록 조회", description = "현재 로그인된 사용자의 루틴 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<RoutineListResponse>> getRoutineList(
            @CurrentUser User currentUser
    ) {
        return ResponseEntity.ok(routineService.getRoutineList(currentUser));
    }

    @Operation(summary = "루틴 상세 조회", description = "특정 루틴의 상세 정보를 조회합니다.")
    @GetMapping("/{routineId}")
    public ResponseEntity<RoutineDetailResponse> getRoutineDetail(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId) {
        return ResponseEntity.ok(routineService.getRoutineDetail(routineId, currentUser));
    }
} 