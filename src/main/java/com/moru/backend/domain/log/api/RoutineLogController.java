package com.moru.backend.domain.log.api;

import com.moru.backend.domain.log.application.RoutineLogService;
import com.moru.backend.domain.log.dto.RoutineLogDetailResponse;
import com.moru.backend.domain.log.dto.RoutineLogSummaryResponse;
import com.moru.backend.domain.log.dto.RoutineStepLogCreateRequest;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/logs")
public class RoutineLogController {
    private final RoutineLogService routineLogService;

    @PostMapping("/{routineId}/logs")
    public ResponseEntity<UUID> startRoutine(
            @PathVariable UUID routineId,
            @CurrentUser User user
    ) {
        UUID routinelogId = routineLogService.startRoutine(user, routineId);
        return ResponseEntity.ok(routinelogId);
    }

    @GetMapping("/{routineLogId}")
    public ResponseEntity<RoutineLogDetailResponse> getRoutineLogDetail(
            @PathVariable UUID routineLogId,
            @CurrentUser User user
    ) {
        RoutineLogDetailResponse response = routineLogService.getRoutineLogDetail(user, routineLogId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{routineLogId}/steps")
    public ResponseEntity<Void> createStepLog(
            @PathVariable UUID routineLogId,
            @CurrentUser User user,
            @RequestBody @Valid RoutineStepLogCreateRequest request
    ) {
        routineLogService.saveStepLog(routineLogId, user, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public ResponseEntity<List<RoutineLogSummaryResponse>> getRoutineLogs(@CurrentUser User user) {
        List<RoutineLogSummaryResponse> logs = routineLogService.getLogs(user);
        return ResponseEntity.ok(logs);
    }
}
