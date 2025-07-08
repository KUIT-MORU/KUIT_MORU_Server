package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineSearchService;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.routine.dto.request.RoutineSearchRequest;
import com.moru.backend.domain.routine.dto.response.RoutineSearchResponse;
import com.moru.backend.domain.routine.dto.response.SearchHistoryResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.annotation.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @Operation(summary = "최근 루틴명 검색 기록 조회", description = "사용자의 최근 루틴명 검색 기록을 조회")
    @GetMapping("/history/routine-name")
    public ResponseEntity<List<SearchHistoryResponse>> getRecentRoutineNameHistory(
            @CurrentUser User currentUser) {
        List<SearchHistoryResponse> histories = routineSearchService.getRecentSearchHistory(currentUser, SearchType.ROUTINE_NAME);
        return ResponseEntity.ok(histories);
    }

    @Operation(summary = "검색 기록 삭제", description = "특정 검색 기록 삭제")
    @DeleteMapping("/history/{historyId}")
    public ResponseEntity<Void> deleteSearchHistory(
            @CurrentUser User currentUser,
            @PathVariable UUID historyId
            ) {
        routineSearchService.deleteSearchHistory(historyId, currentUser);
        return ResponseEntity.ok().build();
    }
}
