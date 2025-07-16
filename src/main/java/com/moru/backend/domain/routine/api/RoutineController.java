package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.meta.dto.response.TagResponse;
import com.moru.backend.domain.routine.application.RoutineAppService;
import com.moru.backend.domain.routine.application.RoutineScheduleService;
import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.application.RoutineTagService;
import com.moru.backend.domain.routine.dto.request.RoutineAppRequest;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.request.RoutineScheduleRequest;
import com.moru.backend.domain.routine.dto.request.RoutineTagConnectRequest;
import com.moru.backend.domain.routine.dto.response.RoutineAppResponse;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.RoutineScheduleResponse;
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
    private final RoutineAppService routineAppService;
    private final RoutineTagService routineTagService;
    private final UserRepository userRepository;
    private final RoutineScheduleService routineScheduleService;

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

    @Operation(summary = "루틴에 앱 연결 추가", description = "루틴에 앱을 연결")
    @PostMapping("/{routineId}/apps")
    public ResponseEntity<List<RoutineAppResponse>> connectAppToRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @Valid @RequestBody RoutineAppRequest request
    ) {
        List<RoutineAppResponse> result = routineAppService.connectAppToRoutine(routineId, request, currentUser);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "루틴에 연결된 앱 목록 조회", description = "루틴에 연결된 앱 목록을 조회합니다.")
    @GetMapping("/{routineId}/apps")
    public ResponseEntity<List<RoutineAppResponse>> getRoutineApps(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId) {
        return ResponseEntity.ok(routineAppService.getRoutineApps(routineId, currentUser));
    }

    @Operation(summary = "루틴 - 앱 연결 해제", description = "루틴에서 앱 연결을 해제합니다.")
    @DeleteMapping("/{routineId}/apps/{appId}")
    public ResponseEntity<Void> disconnectAppFromRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @PathVariable UUID appId) {
        routineAppService.disconnectAppFromRoutine(routineId, appId, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "루틴에 태그 연결 추가", description = "루틴에 여러 태그를 한 번에 연결. 최대 3개까지 연결 가능.")
    @PostMapping("/{routineId}/tags")
    public ResponseEntity<List<TagResponse>> addTagsToRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @Valid @RequestBody RoutineTagConnectRequest request
    ) {
        List<TagResponse> tags = routineTagService.addTagsToRoutine(routineId, request, currentUser);
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "루틴에 연결된 태그 목록 조회", description = "루틴에 연결된 태그 목록을 조회")
    @GetMapping("/{routineId}/tags")
    public ResponseEntity<List<TagResponse>> getRoutineTags(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId
    ) {
        return ResponseEntity.ok(routineTagService.getRoutineTags(routineId, currentUser));
    }

    @Operation(summary = "루틴에 연결된 태그 해제", description = "루틴에서 연결된 태그 연결을 해제함")
    @DeleteMapping("/{routineId}/tags/{tagId}")
    public ResponseEntity<Void> disconnectTagFromRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @PathVariable UUID tagId
    ) {
        routineTagService.disconnectTagFromRoutine(routineId, tagId, currentUser);
        return ResponseEntity.ok().build();
    }

    //====RoutineSchedule====//
    @Operation(summary = "루틴에 스케쥴 추가", description = "이미 존재하는 루틴에 스케쥴(시간대)를 추가하기")
    @PostMapping("/{routineId}/schedules")
    public ResponseEntity<List<RoutineScheduleResponse>> createSchedule(
        @PathVariable UUID routineId,
        @Valid @RequestBody RoutineScheduleRequest request
    ) {
        return ResponseEntity.ok(routineScheduleService.createSchedule(routineId, request));
    }

    @Operation(summary = "루틴 스케쥴 목록 조회", description = "특정 루틴의 스케쥴(시간대) 목록을 조회합니다.")
    @GetMapping("/{routineId}/schedules")
    public ResponseEntity<List<RoutineScheduleResponse>> getRoutineSchedules(
        @PathVariable UUID routineId
    ) {
        return ResponseEntity.ok(routineScheduleService.getRoutineSchedules(routineId));
    }

    @Operation(summary = "특정 루틴 스케쥴 수정", description = "특정 루틴의 스케쥴(시간대)를 수정합니다. 요일 선택, 매일, 주중, 주말 등 반복 방식도 변경 가능.")
    @PatchMapping("/{routineId}/schedules/{schId}")
    public ResponseEntity<List<RoutineScheduleResponse>> updateSchedule(
        @PathVariable UUID routineId,
        @PathVariable UUID schId,
        @Valid @RequestBody RoutineScheduleRequest request
    ) {
        return ResponseEntity.ok(routineScheduleService.updateSchedule(routineId, schId, request));
    }
} 