package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.search.SortType;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.routine.dto.response.SimilarRoutineResponse;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.dto.AuthorInfo;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.RedisKeyUtil;
import com.moru.backend.global.util.S3Service;
import jakarta.annotation.Nullable;
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
    private final RoutineLogRepository routineLogRepository;
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
        // 헬퍼 메서드 사용
        List<Routine> routines = findAndSortRoutinesWithDetails(List.of(routineId));
        if (routines.isEmpty()) {
            throw new CustomException(ErrorCode.ROUTINE_NOT_FOUND);
        }
        Routine routine = routines.get(0);

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

        // 각 서비스에 책임을 위임하여 상태 조회
        boolean isLiked = likeService.isLiked(currentUser.getId(), routineId);
        boolean isScrapped = scrapService.isScrapped(currentUser.getId(), routineId);

        List<SimilarRoutineResponse> similarRoutines = findSimilarRoutines(routine, currentUser);

        User author = routine.getUser();
        AuthorInfo authorInfo = AuthorInfo.from(
                author,
                s3Service.getImageUrl(author.getProfileImageUrl())
        );

        return RoutineDetailResponse.of(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                authorInfo,
                likeCount,
                scrapCount,
                isLiked,
                isScrapped,
                currentUser,
                similarRoutines
        );
    }

    public Page<RoutineListResponse> getRoutineList(
            User user,
            SortType sortType,
            @Nullable DayOfWeek dayOfWeek,
            Pageable pageable
    ) {
        return switch (sortType) {
            case POPULAR -> {
                if (dayOfWeek != null) {
                    yield routineRepository.findRoutinesByUserIdAndDayOfWeekOrderByPopularity(userId, dayOfWeek, pageable);
                } else {
                    User user = userRepository.getReferenceById(userId);
                    yield routineRepository.findDistinctByUserAndStatusIsTrueOrderByLikeCountDescCreatedAtDesc(user, pageable);
                }
            }
            case TIME:
                if (dayOfWeek != null) {
                    // 시나리오 1: 특정 요일이 지정된 경우
                    routinePage = routineRepository.findRoutinesByUserIdAndDayOfWeekOrderByScheduleTimeAsc(user.getId(), dayOfWeek, pageable);
                } else {
                    // 시나리오 2: 요일이 지정되지 않은 경우 (네이티브 쿼리 사용)
                    routinePage = routineRepository.findRoutinesOrderByUpcoming(user.getId(), pageable);
                }
                break;
            case LATEST:
            default:
                routinePage = routineRepository.findDistinctByUserAndStatusIsTrueOrderByCreatedAtDesc(user, pageable);
                break;
        }

        return routinePage.map(this::toRoutineListResponse);
    }


    private List<SimilarRoutineResponse> findSimilarRoutines(Routine routine, User currentUser) {
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

        List<Routine> sortedRoutines = findAndSortRoutinesWithDetails(similarRoutineIds);

        return sortedRoutines.stream()
                .filter(r -> !r.getUser().getId().equals(currentUser.getId()))
                .limit(similarLimitSize)
                .map(r -> SimilarRoutineResponse.from(r, s3Service.getImageUrl(r.getImageUrl())))
                .toList();
    }

    /**
     * ID 목록으로 루틴의 모든 상세 정보 조회, 원본 ID 순서대로 정렬.
     * @param routineIds    조회할 루틴 ID 목록
     * @return              상세 정보가 채워지고 정렬된 Routine 리스트
     */
    public List<Routine> findAndSortRoutinesWithDetails(List<UUID> routineIds) {
        if (routineIds == null || routineIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 분리된 쿼리로 상세 정보 조회
        List<Routine> routinesWithDetails = routineRepository.findWithAllDetailsByIds(routineIds);

        // 2. 원래 ID 순서대로 정렬
        Map<UUID, Routine> routineMap = routinesWithDetails.stream()
                .collect(Collectors.toMap(Routine::getId, Function.identity()));
        return routineIds.stream()
                .map(routineMap::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private RoutineListResponse toRoutineListResponse(Routine routine) {
        return RoutineListResponse.fromRoutine(
                routine,
                s3Service.getImageUrl(routine.getImageUrl()),
                new ArrayList<>(routine.getRoutineTags())
        );
    }

    public boolean isUserVisibleById(UUID routineId) {
        return routineRepository.getIsUserVisibleById(routineId);
    }

    public String getRoutineTitleById(UUID routineId) {
        return routineRepository.findTitleById(routineId);
    }
}
