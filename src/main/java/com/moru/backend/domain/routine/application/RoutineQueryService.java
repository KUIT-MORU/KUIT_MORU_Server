package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.search.SortType;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.RedisKeyUtil;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoutineQueryService {
    private final RoutineRepository routineRepository;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final RedisTemplate<String, String> redisTemplate;
    private final S3Service s3Service;

    @Value("${moru.routine.recommend.similar-fetch-size}")
    private int similarFetchSize;

    @Value("${moru.routine.recommend.similar-limit-size}")
    private int similarLimitSize;

    @Transactional // 조회수 증가 때문에 쓰기 트랜잭션 필요
    public RoutineDetailResponse getRoutineDetail(UUID routineId, User currentUser) {
        // JOIN FETCH로 Routine과 연관 엔티티 (Tag, Step, App) 한번에 조회
        Routine routine = routineRepository.findByIdWithDetails(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        // 1. 자신의 루틴이면 조회수 증가 X
        if (!routine.getUser().getId().equals(currentUser.getId())) {
            // 2, 3. 1분 내 중복 조회 방지 (Redis 사용)
            String redisKey = RedisKeyUtil.routineViewKey(routineId, currentUser.getId());
            Boolean isFirst = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", Duration.ofMinutes(1));
            if (Boolean.TRUE.equals(isFirst)) {
                // 4. 동시성 안전하게 DB에서 직접 증가
                routineRepository.incrementViewCount(routineId);
            }
        }

        int likeCount = likeService.countLikes(routine.getId()).intValue();
        int scrapCount = scrapService.countScrap(routine.getId()).intValue();

        List<RoutineListResponse> similarRoutines = findSimilarRoutines(routine, currentUser);

        return RoutineDetailResponse.of(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                routine.getRoutineTags(), // Fetch된 데이터 사용
                routine.getRoutineSteps(), // Fetch된 데이터 사용
                routine.getRoutineApps(), // Fetch된 데이터 사용
                likeCount,
                scrapCount,
                currentUser,
                similarRoutines
        );
    }

    public Page<RoutineListResponse> getRoutineList(User user, SortType sortType, DayOfWeek dayOfWeek, Pageable pageable) {
        Page<UUID> routineIdPage;
        if (sortType == SortType.TIME && dayOfWeek != null) {
            routineIdPage = routineRepository.findIdsByUserIdAndDayOfWeekOrderByScheduleTimeAsc(user.getId(), dayOfWeek, pageable);
        } else if (sortType == SortType.POPULAR) {
            routineIdPage = routineRepository.findIdsByUserOrderByLikeCountDescCreatedAtDesc(user, pageable);
        } else { // LATEST
            routineIdPage = routineRepository.findIdsByUserOrderByCreatedAtDesc(user, pageable);
        }
        List<UUID> routineIds = routineIdPage.getContent();
        if (routineIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // ID로 상세 정보 조회
        List<Routine> routines = routineRepository.findAllWithDetailsByIds(routineIds);

        // 원래 순서대로 정렬
        Map<UUID, Routine> routineMap = routines.stream()
                .collect(Collectors.toMap(Routine::getId, Function.identity()));
        List<Routine> sortedRoutines = routineIds.stream()
                .map(routineMap::get)
                .filter(Objects::nonNull)
                .toList();

        // DTO로 변환 후 최종 Page 객체 생성
        List<RoutineListResponse> dtoList = sortedRoutines.stream()
                .map(this::toRoutineListResponse)
                .toList();

        return new PageImpl<>(dtoList, pageable, routineIdPage.getTotalElements());
    }


    private List<RoutineListResponse> findSimilarRoutines(Routine routine, User currentUser) {
        if (routine.getRoutineTags().isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> tagIds = routine.getRoutineTags().stream()
                .map(rt -> rt.getTag().getId())
                .toList();
        Pageable pageable = PageRequest.of(0, similarFetchSize); // 넉넉히 조회

        // 이 Repository 메서드도 JOIN FETCH를 사용하도록 수정하면 성능이 더 좋아짐
        Page<UUID> similarRoutineIdPage = routineRepository.findSimilarRoutineIdsByTagIds(tagIds, routine.getId(), pageable);
        List<UUID> similarRoutineIds = similarRoutineIdPage.getContent();

        if (similarRoutineIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 2단계: ID로 상세 정보 조회
        List<Routine> similarRoutines = routineRepository.findAllWithDetailsByIds(similarRoutineIds);

        // 3단계: 원래 순서대로 정렬
        Map<UUID, Routine> routineMap = similarRoutines.stream()
                .collect(Collectors.toMap(Routine::getId, Function.identity()));
        List<Routine> sortedRoutines = similarRoutineIds.stream()
                .map(routineMap::get)
                .filter(Objects::nonNull)
                .toList();

        return sortedRoutines.stream()
                .filter(r -> !r.getUser().getId().equals(currentUser.getId()))
                .limit(similarLimitSize)
                .map(this::toRoutineListResponse)
                .toList();
    }

    private RoutineListResponse toRoutineListResponse(Routine routine) {
        return RoutineListResponse.fromRoutine(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                routine.getRoutineTags()
        );
    }

    public boolean isUserVisibleById(UUID routineId) {
        return routineRepository.getIsUserVisibleById(routineId);
    }

    public String getRoutineTitleById(UUID routineId) {
        return routineRepository.findTitleById(routineId);
    }
}
