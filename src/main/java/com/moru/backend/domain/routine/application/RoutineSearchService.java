package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.dao.SearchHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.search.SearchHistory;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.routine.domain.search.SortType;
import com.moru.backend.domain.routine.dto.request.RoutineSearchRequest;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.RoutineSearchResponse;
import com.moru.backend.domain.routine.dto.response.SearchHistoryResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineSearchService {
    private final RoutineRepository routineRepository;
    private final RoutineTagRepository routineTagRepository;
    private final SearchHistoryRepository searchHistoryRepository;
    private final TagRepository tagRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineQueryService routineQueryService;
    private final S3Service s3Service;

    /**
     * 루틴 검색 비즈니스 로직 수행
     *
     * @param request (검색 키워드, 태그 리스트, 페이징 정보)
     * @return 페이징 처리된 루틴 검색 결과
     */
    @Transactional(readOnly = true)
    public Page<RoutineSearchResponse> searchRoutines(RoutineSearchRequest request, User user) {
        // 페이징 정보 생성 (# page, size)
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 1단계: ID 조회 (변경된 Repository 메서드 호출)
        Page<UUID> routineIdPage;
        if (request.getSortType() == SortType.POPULAR) {
            routineIdPage = routineRepository.findIdsBySearchCriteriaOrderByLikeCount(
                    request.getTitleKeyword(),
                    request.getTagNames(),
                    pageable
            );
        } else {
            routineIdPage = routineRepository.findIdsBySearchCriteriaOrderByCreatedAt(
                    request.getTitleKeyword(),
                    request.getTagNames(),
                    pageable
            );
        }

        List<UUID> routineIds = routineIdPage.getContent();
        if (routineIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2단계: ID로 상세 정보 조회
        List<Routine> sortedRoutines = routineQueryService.findAndSortRoutinesWithDetails(routineIds);

        // 3단계: "소유주에 의해 실행 중인" 루틴 정보 조회
        // 3-1. 검색된 루틴들의 소유자 ID 목록 추출
        List<UUID> ownerIds = sortedRoutines.stream()
                .map(routine -> routine.getUser().getId())
                .distinct()
                .toList();

        // 3-2. 소유자 목록으로 현재 실행 중인 모든 로그를 DB에서 한 번에 조회
        List<RoutineLog> activeLogs = routineLogRepository.findActiveLogsForUsers(ownerIds);

        // 3-3. "소유주에 의해 실행 중인" 루틴의 ID만 필터링하여 Set으로
        Map<UUID, UUID> routineToOwnerMap = sortedRoutines.stream()
                .collect(Collectors.toMap(Routine::getId, r -> r.getUser().getId()));

        Set<UUID> runningByOwnerRoutineIds = activeLogs.stream()
                .filter(log -> {
                    UUID routineId = log.getRoutineSnapshot().getOriginalRoutineId();
                    UUID runnerId = log.getUser().getId();
                    // 이 로그를 실행한 사람(runnerId)이 이 루틴의 소유주(owner)인지 확인
                    return runnerId.equals(routineToOwnerMap.get(routineId));
                })
                .map(log -> log.getRoutineSnapshot().getOriginalRoutineId())
                .collect(Collectors.toSet());

        // 4단계: DTO로 변환
        List<RoutineSearchResponse> dtoList = sortedRoutines.stream()
                .map(routine -> {
                    boolean isRunning = runningByOwnerRoutineIds.contains(routine.getId());
                    RoutineListResponse routineListResponse = RoutineListResponse.fromRoutine(
                            routine,
                            s3Service.getImageUrl(routine.getImageUrl()),
                            new ArrayList<>(routine.getRoutineTags()), // 이미 Fetch 되어 있음
                            isRunning
                    );
                    return RoutineSearchResponse.of(routineListResponse, isRunning);
                })
                .toList();

        // 5단계: 최종 Page 객체 생성
        return new PageImpl<>(dtoList, pageable, routineIdPage.getTotalElements());
    }

    /**
     * 사용자의 검색 기록을 저장
     *
     * @param keyword    사용자가 검색한 키워드
     * @param searchType 검색 유형 (루틴명 검색 or 태그명 검색)
     * @param user       검색을 수행한 사용자 엔티티
     */
    @Transactional
    public void saveSearchHistory(String keyword, SearchType searchType, User user) {
//        // 디버깅을 위한 로그 추가
//        System.out.println("=== SearchHistory 저장 디버깅 ===");
//        System.out.println("User: " + user);
//        System.out.println("User ID: " + (user != null ? user.getId() : "null"));
//        System.out.println("Keyword: " + keyword);
//        System.out.println("SearchType: " + searchType);

        if (user == null) {
            throw new IllegalArgumentException("사용자 정보가 null입니다.");
        }

        if (user.getId() == null) {
            throw new IllegalArgumentException("사용자 ID가 null입니다.");
        }
        SearchHistory searchHistory = SearchHistory.builder()
                .userId(user.getId())
                .searchKeyword(keyword)
                .searchType(searchType)
                .build();
        System.out.println("SearchHistory to save: " + searchHistory);
        searchHistoryRepository.save(searchHistory);
        System.out.println("SearchHistory saved successfully");
    }

    @Transactional(readOnly = true)
    public List<SearchHistoryResponse> getRecentSearchHistory(User user, SearchType searchType) {
        List<SearchHistory> histories = searchHistoryRepository
                .findByUserIdAndSearchTypeOrderByCreatedAtDesc(user.getId(), searchType);
        return histories.stream()
                .limit(12) // todo : 검색기록 개수 제한 몇개인지 check 
                .map(history -> SearchHistoryResponse.builder()
                        .id(history.getId())
                        .searchKeyword(history.getSearchKeyword())
                        .searchType(history.getSearchType().name())
                        .createdAt(history.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteSearchHistory(UUID historyId, User user) {
        SearchHistory history = searchHistoryRepository.findById(historyId)
                .orElseThrow(() -> new CustomException(ErrorCode.HISTORY_NOT_FOUND));

        if (!history.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.USER_NOT_MATCH);
        }

        searchHistoryRepository.delete(history);
    }

    @Transactional
    public void deleteAllSearchHistory(User user, SearchType searchType) {
        List<SearchHistory> histories = searchHistoryRepository
                .findByUserIdAndSearchTypeOrderByCreatedAtDesc(user.getId(), searchType);
        searchHistoryRepository.deleteAll(histories);
    }

    @Transactional(readOnly = true)
    public List<String> getRoutineTitleSuggestions(String keyword) {
        return routineRepository.findTitleSuggestions(keyword);
    }
}
