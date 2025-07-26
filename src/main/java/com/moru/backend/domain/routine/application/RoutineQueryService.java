package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class RoutineQueryService {
    private final RoutineRepository routineRepository;
    private final RoutineStepRepository routineStepRepository;
    private final RoutineAppRepository routineAppRepository;
    private final LikeService likeService;
    private final ScrapService scrapService;
    private final RedisTemplate<String, String> redisTemplate;
    private final S3Service s3Service;

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

    public Page<RoutineListResponse> getRoutineList(User user, String sortType, DayOfWeek dayOfWeek, Pageable pageable) {
        Page<Routine> routines;
        if ("TIME".equals(sortType) && dayOfWeek != null) {
            routines = routineRepository.findByUserIdAndDayOfWeekOrderByScheduleTimeAsc(user.getId(), dayOfWeek, pageable);
        } else if ("POPULAR".equals(sortType)) {
            routines = routineRepository.findByUserOrderByLikeCountDescCreatedAtDesc(user, pageable);
        } else {
            routines = routineRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        }
        return routines.map(this::toRoutineListResponse);
    }


    public List<RoutineListResponse> getHotRoutines(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        double viewWeight = 0.5; // 기본값, 추후 외부에서 주입 가능
        double likeWeight = 0.5; // 기본값, 추후 외부에서 주입 가능
        return routineRepository.findHotRoutines(weekAgo, viewWeight, likeWeight, PageRequest.of(0, limit)).stream()
                .map(this::toRoutineListResponse)
                .toList();
    }

    private List<RoutineListResponse> findSimilarRoutines(Routine routine, User currentUser) {
        if (routine.getRoutineTags().isEmpty()) {
            return List.of();
        }
        List<UUID> tagIds = routine.getRoutineTags().stream()
                .map(rt -> rt.getTag().getId())
                .toList();
        Pageable pageable = PageRequest.of(0, 20); // 넉넉히 조회

        // 이 Repository 메서드도 JOIN FETCH를 사용하도록 수정하면 성능이 더 좋아짐
        List<Routine> routines = routineRepository.findSimilarRoutinesByTagIds(tagIds, routine.getId(), pageable);

        return routines.stream()
                .filter(r -> !r.getUser().getId().equals(currentUser.getId()))
                .limit(10)
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
}
