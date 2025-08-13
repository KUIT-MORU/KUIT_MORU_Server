package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineScheduleService;
import com.moru.backend.domain.routine.dto.request.RoutineScheduleRequest;
import com.moru.backend.domain.routine.dto.response.RoutineScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/routines/{routineId}/schedules")
@RequiredArgsConstructor
@Tag(name = "루틴-스케줄 관리", description = "특정 루틴의 스케줄(시간대) 관련 API")
public class RoutineScheduleController {
    private final RoutineScheduleService routineScheduleService;

    @Operation(summary = "루틴에 스케쥴 추가", description = "이미 존재하는 루틴에 스케쥴(시간대)를 추가하기")
    @PostMapping
    public ResponseEntity<List<RoutineScheduleResponse>> createSchedule(
            @PathVariable UUID routineId,
            @Valid @RequestBody RoutineScheduleRequest request
    ) {
        return ResponseEntity.ok(routineScheduleService.createSchedule(routineId, request));
    }

    @Operation(summary = "루틴 스케쥴 목록 조회", description = "특정 루틴의 스케쥴(시간대) 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<RoutineScheduleResponse>> getRoutineSchedules(
            @PathVariable UUID routineId
    ) {
        return ResponseEntity.ok(routineScheduleService.getRoutineSchedules(routineId));
    }

    @Operation(summary = "특정 루틴 스케쥴 수정", description = "특정 루틴의 스케쥴(시간대)를 수정합니다. 요일 선택, 매일, 주중, 주말 등 반복 방식도 변경 가능.")
    @PatchMapping("/{schId}")
    public ResponseEntity<List<RoutineScheduleResponse>> updateSchedule(
            @PathVariable UUID routineId,
            @PathVariable UUID schId,
            @Valid @RequestBody RoutineScheduleRequest request
    ) {
        return ResponseEntity.ok(routineScheduleService.updateSchedule(routineId, schId, request));
    }

    @Operation(summary = "특정 루틴 스케쥴 삭제", description = "특정 루틴의 스케쥴(시간대)를 삭제합니다.")
    @DeleteMapping("/{schId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable UUID routineId,
            @PathVariable UUID schId
    ) {
        routineScheduleService.deleteSchedule(routineId, schId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "루틴 스케쥴 전체 초기화", description = "특정 루틴에 할당된 모든 스케쥴(시간대)를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<Void> deleteAllSchedules(
            @PathVariable UUID routineId
    ) {
        routineScheduleService.deleteAllSchedules(routineId);
        return ResponseEntity.ok().build();
    }
}
