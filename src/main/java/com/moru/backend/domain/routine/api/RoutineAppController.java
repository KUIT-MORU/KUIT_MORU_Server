package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineAppService;
import com.moru.backend.domain.routine.dto.request.RoutineAppRequest;
import com.moru.backend.domain.routine.dto.response.RoutineAppResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routines/{routineId}/apps")
@RequiredArgsConstructor
@Tag(name = "루틴-앱", description = "루틴-앱 관련 API")
public class RoutineAppController {

    private final RoutineAppService routineAppService;

    @Operation(summary = "루틴에 앱 연결 추가", description = "루틴에 앱을 연결")
    @PostMapping
    public ResponseEntity<List<RoutineAppResponse>> connectAppToRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @Valid @RequestBody RoutineAppRequest request
            ) {
        List<RoutineAppResponse> result = routineAppService.connectAppToRoutine(routineId, request, currentUser);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "루틴에 연결된 앱 목록 조회", description = "루틴에 연결된 앱 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<RoutineAppResponse>> getRoutineApps(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId) {
        return ResponseEntity.ok(routineAppService.getRoutineApps(routineId, currentUser));
    }

    @Operation(summary = "루틴 - 앱 연결 해제", description = "루틴에서 앱 연결을 해제합니다.")
    @DeleteMapping("/{appId}")
    public ResponseEntity<Void> disconnectAppFromRoutine(
            @CurrentUser User currentUser,
            @PathVariable UUID routineId,
            @PathVariable UUID appId) {
        routineAppService.disconnectAppFromRoutine(routineId, appId, currentUser);
        return ResponseEntity.ok().build();
    }
}
