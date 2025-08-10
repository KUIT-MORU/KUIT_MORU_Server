package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.log.application.RoutineLogService;
import com.moru.backend.domain.log.dto.LiveUserResponse;
import com.moru.backend.domain.meta.dto.response.TagResponse;
import com.moru.backend.domain.routine.application.*;
import com.moru.backend.domain.routine.domain.search.SortType;
import com.moru.backend.domain.routine.dto.request.*;
import com.moru.backend.domain.routine.dto.response.*;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@RestController
@RequestMapping("/api/routines")
@RequiredArgsConstructor
@Tag(name = "루틴", description = "루틴 관련 API")
public class RoutineController {
    private final RoutineCommandService routineCommandService;
    private final RoutineQueryService routineQueryService;
    private final RoutineRecommendService routineRecommendService;
    private final RoutineAppService routineAppService;
    private final RoutineTagService routineTagService;
    private final RoutineScheduleService routineScheduleService;
    private final RoutineLogService routineLogService;

    @Operation(summary = "루틴 생성", description = "새로운 루틴을 생성합니다.")
    @PostMapping
    public ResponseEntity<RoutineCreateResponse> createRoutine(
            @CurrentUser User currentUser,
            @Valid @RequestBody RoutineCreateRequest request) {
        return ResponseEntity.ok(routineCommandService.createRoutine(request, currentUser));
    }

    @Operation(summary = "내 루틴 목록 조회", description = "현재 로그인된 사용자의 루틴 목록을 조회합니다. sortType: LATEST(최신순), POPULAR(인기순), TIME(시간순, dayOfWeek 필요)")
    @GetMapping
    public ResponseEntity<Page<RoutineListResponse>> getRoutineList(
            @CurrentUser User currentUser,
            @RequestParam(value = "sortType", defaultValue = "TIME") SortType sortType,
            @RequestParam(value = "dayOfWeek", required = false) DayOfWeek dayOfWeek,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(routineQueryService.getRoutineList(currentUser, sortType, dayOfWeek, PageRequest.of(page, size)));
    }

    @Operation(summary = "루틴 상세 조회", description = "특정 루틴의 상세 정보를 조회합니다.")
    @GetMapping("/{routineId}")
    public ResponseEntity<RoutineDetailResponse> getRoutineDetail(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId) {
        return ResponseEntity.ok(routineQueryService.getRoutineDetail(routineId, currentUser));
    }

    @Operation(summary = "루틴 수정", description = "특정 루틴의 정보를 수정합니다.")
    @PatchMapping("/{routineId}")
    public ResponseEntity<Void> updateRoutine(
        @CurrentUser User currentUser,
        @PathVariable UUID routineId,
        @Valid @RequestBody RoutineUpdateRequest request
    ) {
        routineCommandService.updateRoutine(routineId, request, currentUser);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "루틴 삭제", description = "특정 루틴을 삭제합니다.")
    @DeleteMapping("/{routineId}")
    public ResponseEntity<Void> deleteRoutine(
        @CurrentUser User currentUser,
        @PathVariable UUID routineId
    ) {
        routineCommandService.deleteRoutine(routineId, currentUser);
        return ResponseEntity.ok().build();
    }
} 