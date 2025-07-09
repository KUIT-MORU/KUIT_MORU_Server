package com.moru.backend.domain.log.api;

import com.moru.backend.domain.log.application.RoutineLogService;
import com.moru.backend.domain.log.dto.RoutineLogDetailResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.annotation.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
