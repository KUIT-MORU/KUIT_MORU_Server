package com.moru.backend.domain.log.api;

import com.moru.backend.domain.log.application.RoutineLogService;
import com.moru.backend.domain.log.dto.RoutineLogDetailResponse;
import com.moru.backend.domain.log.dto.RoutineLogEndRequest;
import com.moru.backend.domain.log.dto.RoutineLogSummaryResponse;
import com.moru.backend.domain.log.dto.RoutineStepLogCreateRequest;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
public class RoutineLogController {
    private final RoutineLogService routineLogService;

    @Operation(summary = "루틴 로그 생성")
    @PostMapping("/{routineId}/logs")
    public ResponseEntity<UUID> startRoutine(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        UUID routinelogId = routineLogService.startRoutine(user, routineId);
        return ResponseEntity.ok(routinelogId);
    }

    @Operation(summary = "루틴 로그 상세 조회")
    @GetMapping("/{routineLogId}")
    public ResponseEntity<RoutineLogDetailResponse> getRoutineLogDetail(
            @PathVariable UUID routineLogId,
            @CurrentUser User user
    ) {
        RoutineLogDetailResponse response = routineLogService.getRoutineLogDetail(user, routineLogId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "루틴 스텝 로그 생성")
    @PostMapping("/{routineLogId}/steps")
    public ResponseEntity<Void> createStepLog(
            @PathVariable UUID routineLogId,
            @CurrentUser User user,
            @RequestBody @Valid RoutineStepLogCreateRequest request
    ) {
        routineLogService.saveStepLog(routineLogId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "루틴 로그 종료")
    @PostMapping("/{routineLogId}/end")
    public ResponseEntity<Void> endRoutine(
            @PathVariable UUID routineLogId,
            @RequestBody RoutineLogEndRequest request,
            @CurrentUser User user
    ) {
        routineLogService.endRoutine(routineLogId, request, user);
        return ResponseEntity.ok().build();
    }

//    @Operation(summary = "루틴 로그 목록 조회")
//    @GetMapping
//    public ResponseEntity<List<RoutineLogSummaryResponse>> getRoutineLogs(@CurrentUser User user) {
//        List<RoutineLogSummaryResponse> logs = routineLogService.getLogs(user);
//        return ResponseEntity.ok(logs);
//    }

    @Operation(summary = "오늘 실행한 루틴 로그 조회")
    @GetMapping("/today")
    public ResponseEntity<List<RoutineLogSummaryResponse>> getTodayLogs(@CurrentUser User user) {
        return ResponseEntity.ok(routineLogService.getTodayLogs(user));
    }

    @Operation(summary = "최근 7일간 실행한 루틴 로그 조회")
    @GetMapping("/recent")
    public ResponseEntity<List<RoutineLogSummaryResponse>> getRecentLogs(@CurrentUser User user) {
        return ResponseEntity.ok(routineLogService.getRecentLogs(user));
    }

    @Operation(summary = "무한 스크롤용 전체 로그(페이지 포함)")
    @GetMapping
    public ResponseEntity<List<RoutineLogSummaryResponse>> getRoutineLogs(
            @CurrentUser User user,
            @RequestParam(defaultValue = "0") Integer offset,
            @RequestParam(defaultValue = "10") Integer limit
    ) {
        return ResponseEntity.ok(routineLogService.getLogs(user, offset, limit));
    }

}
