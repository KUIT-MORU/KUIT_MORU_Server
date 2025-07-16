package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineScheduleRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.RecommendFeedResponse;
import com.moru.backend.domain.routine.dto.response.RoutineCreateResponse;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.RoutineValidator;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.dto.request.RoutineUpdateRequest;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import java.time.LocalDateTime;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.routine.domain.ActionType;
import org.springframework.data.domain.PageRequest;
import java.util.Map;
import java.util.stream.Collectors;
import com.moru.backend.domain.routine.dto.response.TagPairSection;
import com.moru.backend.domain.routine.dao.TagPairCount;

@Service
@RequiredArgsConstructor
public class RoutineService {
    private final RoutineRepository routineRepository;
    private final RoutineStepRepository routineStepRepository;
    private final RoutineTagRepository routineTagRepository;
    private final RoutineAppRepository routineAppRepository;
    private final RoutineScheduleRepository routineScheduleRepository;
    private final TagRepository tagRepository;
    private final AppRepository appRepository;
    private final RoutineValidator routineValidator;
    private final RoutineUserActionRepository routineUserActionRepository;
    // 추가: ScrapService, LikeService DI
    private final ScrapService scrapService;
    private final LikeService likeService;
    private final UserFavoriteTagRepository userFavoriteTagRepository;

    @Transactional
    public RoutineCreateResponse createRoutine(RoutineCreateRequest request, User user) {
        // 검증용 로그 추가
        boolean isSimple = request.isSimple();
        Duration totalTime = null;
        if (!isSimple) {
            totalTime = request.steps().stream()
                .map(step -> {
                    Duration stepTime = step.estimatedTime() != null ? step.estimatedTime() : Duration.ZERO;
                    return stepTime;
                })
                .reduce(Duration.ZERO, (acc, stepTime) -> {
                    Duration result = acc.plus(stepTime);
                    return result;
                });
        }
        
        // 루틴 엔티티 생성 및 저장 
        Routine routine = Routine.builder()
            .id(UUID.randomUUID())
            .user(user)
            .title(request.title())
            .isSimple(isSimple)
            .isUserVisible(request.isUserVisible())
            .likeCount(0)
            .content(Optional.ofNullable(request.description()).orElse(""))
            .requiredTime(isSimple ? null : totalTime)
            .status(true)
            .build();
        Routine savedRoutine = routineRepository.save(routine);

        // 태그 저장 (최대 3개)
        List<RoutineTag> routineTags = request.tags().stream()
                .map(tagName -> {
                    // 기존 태그가 존재하면 재사용, 없으면 생성 
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    return RoutineTag.builder()
                            .routine(savedRoutine)
                            .tag(tag)
                            .build();
                })
                .toList();
        routineTagRepository.saveAll(routineTags);

        // 스텝 저장 (최대 6개)
        List<RoutineStep> routineSteps = request.steps().stream()
            .map(stepReq -> {
                RoutineStep.RoutineStepBuilder builder = RoutineStep.builder()
                        .routine(savedRoutine)
                        .name(stepReq.name())
                        .stepOrder(stepReq.stepOrder());
                if (!isSimple && stepReq.estimatedTime() != null) {
                    builder.estimatedTime(stepReq.estimatedTime());
                }
                return builder.build();
            })
            .toList();
        routineStepRepository.saveAll(routineSteps);
        // 앱 저장 (최대 4개) - 해당 루틴 실행시 제한되는 앱 목록
        List<RoutineApp> routineApps = List.of();
        if (request.selectedApps() != null && !request.selectedApps().isEmpty()) {
            routineApps = request.selectedApps().stream()
                    .map(pkg -> {
                        // findOrCreateApp
                        App app = appRepository.findByPackageName(pkg)
                                .orElseGet(() -> appRepository.save(
                                        App.builder()
                                                .packageName(pkg)
                                                .name(pkg) // name에도 packageName을 임시로 넣음
                                                .build()
                                ));
                        return RoutineApp.builder()
                                .routine(savedRoutine)
                                .app(app)
                                .build();
                    })
                    .toList();
            routineAppRepository.saveAll(routineApps);
        }
        // 생성 응답은 최소 정보만 반환
        return new RoutineCreateResponse(
                savedRoutine.getId(),
                savedRoutine.getTitle(),
                savedRoutine.getCreatedAt()
        );
    }

    @Transactional
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

    private RoutineListResponse toRoutineListResponse(Routine routine) {
        List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
        Long likeCount = routineUserActionRepository.countByRoutineIdAndActionType(routine.getId(), ActionType.LIKE);
        return new RoutineListResponse(
                routine.getId(),
                routine.getTitle(),
                routine.getImageUrl(),
                tags.stream().map(rt -> rt.getTag().getName()).toList(),
                likeCount.intValue(),
                routine.getCreatedAt(),
                routine.getRequiredTime()
        );
    }

    @Transactional
    public RoutineDetailResponse getRoutineDetail(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        // 조회수 증가
        routine.setViewCount(routine.getViewCount() + 1);
        routineRepository.save(routine);
        List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
        List<RoutineStep> steps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        List<RoutineApp> apps = routineAppRepository.findByRoutine(routine);

        // 서비스 사용하도록 변경
        int likeCount = likeService.countLikes(routine.getId()).intValue();
        int scrapCount = scrapService.countScrap(routine.getId()).intValue();
        return RoutineDetailResponse.of(routine, tags, steps, apps, likeCount, scrapCount, currentUser);
    }

    @Transactional
    public RoutineDetailResponse updateRoutine(UUID routineId, RoutineUpdateRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);

        // 단순 필드 수정
        updateSimpleFields(routine, request);

        // 태그, 스텝, 앱 교체
        if (request.tags() != null) updateTags(routine, request.tags());
        if (request.steps() != null) updateSteps(routine, request.steps());
        if (request.selectedApps() != null) updateApps(routine, request.selectedApps());

        // 응답 생성
        List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
        List<RoutineStep> steps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        List<RoutineApp> apps = routineAppRepository.findByRoutine(routine);
        int likeCount = routineUserActionRepository.countByRoutineIdAndActionType(routine.getId(), ActionType.LIKE).intValue();
        int scrapCount = routineUserActionRepository.countByRoutineIdAndActionType(routine.getId(), ActionType.SCRAP).intValue();
        return RoutineDetailResponse.of(routine, tags, steps, apps, likeCount, scrapCount, currentUser);
    }

    @Transactional
    public void deleteRoutine(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        routineRepository.delete(routine);
    }

    @Transactional
    public RecommendFeedResponse getRecommendFeed(User user) {
        List<RoutineListResponse> hotRoutines = getHotRoutines(10);
        List<RoutineListResponse> personalRoutines = getPersonalRoutines(user, 10);
        // 전체 루틴에서 가장 많이 함께 쓰인 태그쌍 추천 
        TagPairSection tagPairSection1 = getTopTagPairSection(10);
        // 관심태그와 함께 쓰인 태그쌍 추천
        TagPairSection tagPairSection2 = getInterestTagPairSection(user, 10);
        return new RecommendFeedResponse(hotRoutines, personalRoutines, tagPairSection1, tagPairSection2);
    }

    @Transactional
    public List<RoutineListResponse> getHotRoutines(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        List<Routine> routines = routineRepository.findHotRoutines(weekAgo, PageRequest.of(0, limit));
        return routines.stream()
            .map(this::toRoutineListResponse)
            .toList();
    }

    @Transactional
    public List<RoutineListResponse> getPersonalRoutines(User user, int limit) {
        // 1. 관심태그
        List<UserFavoriteTag> favoriteTags = userFavoriteTagRepository.findAllByUserId(user.getId());
        List<String> tagNames = new ArrayList<>();
        tagNames.addAll(favoriteTags.stream().map(f -> f.getTag().getName()).toList());

        // 2. 내 루틴 태그
        List<Routine> myRoutines = routineRepository.findAllByUser(user);
        for (Routine r : myRoutines) {
            List<RoutineTag> tags = routineTagRepository.findByRoutine(r);
            tagNames.addAll(tags.stream().map(rt -> rt.getTag().getName()).toList());
        }

        // 3. 태그 count 집계
        Map<String, Long> tagCount = tagNames.stream()
            .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        // 4. count 높은 순 정렬
        List<String> sortedTags = tagCount.entrySet().stream()
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .map(Map.Entry::getKey)
            .toList();

        // 5. 태그가 많이 포함된 루틴 추천
        if (!sortedTags.isEmpty()) {
            List<Routine> routines = routineRepository.findRoutinesByTagsOrderByTagCount(sortedTags, PageRequest.of(0, limit));
            return routines.stream().map(this::toRoutineListResponse).toList();
        }

        // 6. 관심태그+내루틴이 없으면 → 스크랩한 루틴의 태그로 재시도
        List<RoutineUserAction> scraps = routineUserActionRepository.findAllByUserIdAndActionType(user.getId(), ActionType.SCRAP);
        List<String> scrapTagNames = new java.util.ArrayList<>();
        for (RoutineUserAction scrap : scraps) {
            List<RoutineTag> tags = routineTagRepository.findByRoutine(scrap.getRoutine());
            scrapTagNames.addAll(tags.stream().map(rt -> rt.getTag().getName()).toList());
        }
        if (!scrapTagNames.isEmpty()) {
            Map<String, Long> scrapTagCount = scrapTagNames.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
            List<String> sortedScrapTags = scrapTagCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
            List<Routine> routines = routineRepository.findRoutinesByTagsOrderByTagCount(sortedScrapTags, PageRequest.of(0, limit));
            return routines.stream().map(this::toRoutineListResponse).toList();
        }

        // 7. 그래도 없으면 → 핫한 루틴
        return getHotRoutines(limit);
    }

    @Transactional
    public TagPairSection getTopTagPairSection(int limit) {
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        if (topPairs.isEmpty()) return null;
        TagPairCount pair = topPairs.get(0);
        List<Routine> routines = routineRepository.findRoutinesByTagPair(
            UUID.fromString(pair.getTag1()), UUID.fromString(pair.getTag2()), PageRequest.of(0, limit));
        String tagName1 = tagRepository.findById(UUID.fromString(pair.getTag1())).map(Tag::getName).orElse("");
        String tagName2 = tagRepository.findById(UUID.fromString(pair.getTag2())).map(Tag::getName).orElse("");
        return new TagPairSection(tagName1, tagName2, routines.stream().map(this::toRoutineListResponse).toList());
    }

    @Transactional
    public TagPairSection getInterestTagPairSection(User user, int limit) {
        List<UserFavoriteTag> favoriteTags = userFavoriteTagRepository.findAllByUserId(user.getId());
        Set<String> interestTagIds = favoriteTags.stream().map(f -> f.getTag().getId().toString()).collect(Collectors.toSet());
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        for (TagPairCount pair : topPairs) {
            if (interestTagIds.contains(pair.getTag1()) || interestTagIds.contains(pair.getTag2())) {
                List<Routine> routines = routineRepository.findRoutinesByTagPair(
                    UUID.fromString(pair.getTag1()), UUID.fromString(pair.getTag2()), PageRequest.of(0, limit));
                String tagName1 = tagRepository.findById(UUID.fromString(pair.getTag1())).map(Tag::getName).orElse("");
                String tagName2 = tagRepository.findById(UUID.fromString(pair.getTag2())).map(Tag::getName).orElse("");
                return new TagPairSection(tagName1, tagName2, routines.stream().map(this::toRoutineListResponse).toList());
            }
        }
        return null;
    }

    private void updateSimpleFields(Routine routine, RoutineUpdateRequest request) {
        if (request.title() != null) routine.setTitle(request.title());
        if (request.description() != null) routine.setContent(request.description());
        if (request.imageUrl() != null) routine.setImageUrl(request.imageUrl());
        if (request.isUserVisible() != null) routine.setUserVisible(request.isUserVisible());
        if (request.isSimple() != null) routine.setSimple(request.isSimple());
    }

    private void updateTags(Routine routine, List<String> tags) {
        routineTagRepository.deleteByRoutine(routine);
        List<RoutineTag> newTags = tags.stream()
            .map(tagName -> {
                Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                return RoutineTag.builder().routine(routine).tag(tag).build();
            }).toList();
        routineTagRepository.saveAll(newTags);
    }

    private void updateSteps(Routine routine, List<RoutineStepRequest> steps) {
        routineStepRepository.deleteByRoutine(routine);
        List<RoutineStep> newSteps = steps.stream()
            .map(stepReq -> RoutineStep.builder()
                .routine(routine)
                .name(stepReq.name())
                .stepOrder(stepReq.stepOrder())
                .estimatedTime(stepReq.estimatedTime())
                .build())
            .toList();
        routineStepRepository.saveAll(newSteps);
    }

    private void updateApps(Routine routine, List<String> selectedApps) {
        routineAppRepository.deleteByRoutine(routine);
        List<RoutineApp> newApps = selectedApps.stream()
            .map(pkg -> {
                App app = appRepository.findByPackageName(pkg)
                    .orElseGet(() -> appRepository.save(App.builder().packageName(pkg).name(pkg).build()));
                return RoutineApp.builder().routine(routine).app(app).build();
            }).toList();
        routineAppRepository.saveAll(newApps);
    }
}
