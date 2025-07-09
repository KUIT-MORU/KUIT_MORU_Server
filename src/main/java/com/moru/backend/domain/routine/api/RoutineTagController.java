package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.meta.dto.response.TagResponse;
import com.moru.backend.domain.routine.application.RoutineTagService;
import com.moru.backend.domain.routine.dto.request.RoutineTagConnectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routines/{routineId}/tags")
@RequiredArgsConstructor
@Tag(name = "루틴-태그", description = "루틴과 태그 연결/조회/해제 API")
public class RoutineTagController {
    private final RoutineTagService routineTagService;

    @Operation(summary = "루틴에 태그 연결 추가", description = "루틴에 여러 태그를 한 번에 연결. 최대 3개까지 연결 가능.")
    @PostMapping
    public ResponseEntity<List<TagResponse>> addTagsToRoutine(
            @PathVariable UUID routineId,
            @RequestBody RoutineTagConnectRequest request
            ) {
        List<TagResponse> tags = routineTagService.addTagsToRoutine(routineId, request.tagIds());
        return ResponseEntity.ok(tags);
    }
}
