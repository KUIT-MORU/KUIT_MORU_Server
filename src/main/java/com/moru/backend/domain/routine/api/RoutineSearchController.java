package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineSearchService;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.routine.dto.request.RoutineSearchRequest;
import com.moru.backend.domain.routine.dto.response.RoutineSearchResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/routines/search")
@RequiredArgsConstructor
@Tag(name = "루틴 검색", description = "루틴 검색 관련 API")
public class RoutineSearchController {
    private final RoutineSearchService routineSearchService;

    @Operation(summary = "루틴 검색", description = "루틴명과 태그명을 기반으로 루틴을 검색")
    @PostMapping
    public ResponseEntity<Page<RoutineSearchResponse>> searchRoutines(
            @CurrentUser User currentUser,
            @RequestBody RoutineSearchRequest request
    ) {
        // 사용자가 request - titleKeyword로 검색했으면, 검색 기록 저장하기
        if (request.getTitleKeyword() != null && !request.getTitleKeyword().trim().isEmpty()) {
            routineSearchService.saveSearchHistory(
                    request.getTitleKeyword(),
                    SearchType.ROUTINE_NAME, // 루틴명 검색 기록 유형
                    currentUser
            );
        }
        // 실제 검색 로직 수행하고, 페이징된 결과 반환하기
        Page<RoutineSearchResponse> result = routineSearchService.searchRoutines(request);
        return ResponseEntity.ok(result);
    }
}
