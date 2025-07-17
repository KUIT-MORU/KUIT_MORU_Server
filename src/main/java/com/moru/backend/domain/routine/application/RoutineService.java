package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.*;
import com.moru.backend.domain.routine.domain.*;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.routine.dto.request.*;
import com.moru.backend.domain.routine.dto.response.*;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import com.moru.backend.domain.social.application.LikeService;
import com.moru.backend.domain.social.application.ScrapService;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.global.validator.RoutineValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ScrapService scrapService;
    private final LikeService likeService;
    private final UserFavoriteTagRepository userFavoriteTagRepository;

    // ========================= 루틴 생성/수정/삭제 =========================

    @Transactional
    public RoutineCreateResponse createRoutine(RoutineCreateRequest request, User user) {
        boolean isSimple = request.isSimple();
        Duration totalTime = isSimple ? null : request.steps().stream()
                .map(step -> Optional.ofNullable(step.estimatedTime()).orElse(Duration.ZERO))
                .reduce(Duration.ZERO, Duration::plus);

        Routine routine = Routine.builder()
                .id(UUID.randomUUID())
                .user(user)
                .title(request.title())
                .isSimple(isSimple)
                .isUserVisible(request.isUserVisible())
                .likeCount(0)
                .content(Optional.ofNullable(request.description()).orElse(""))
                .requiredTime(totalTime)
                .status(true)
                .build();
        Routine savedRoutine = routineRepository.save(routine);

        saveRoutineTags(savedRoutine, request.tags());
        saveRoutineSteps(savedRoutine, request.steps(), isSimple);
        saveRoutineApps(savedRoutine, request.selectedApps());

        return new RoutineCreateResponse(savedRoutine.getId(), savedRoutine.getTitle(), savedRoutine.getCreatedAt());
    }

    @Transactional
    public RoutineDetailResponse getRoutineDetail(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        routine.setViewCount(routine.getViewCount() + 1);
        routineRepository.save(routine);

        List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
        List<RoutineStep> steps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        List<RoutineApp> apps = routineAppRepository.findByRoutine(routine);

        int likeCount = likeService.countLikes(routine.getId()).intValue();
        int scrapCount = scrapService.countScrap(routine.getId()).intValue();
        return RoutineDetailResponse.of(routine, tags, steps, apps, likeCount, scrapCount, currentUser);
    }

    @Transactional
    public RoutineDetailResponse updateRoutine(UUID routineId, RoutineUpdateRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        updateSimpleFields(routine, request);
        if (request.tags() != null) updateTags(routine, request.tags());
        if (request.steps() != null) updateSteps(routine, request.steps());
        if (request.selectedApps() != null) updateApps(routine, request.selectedApps());

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

    // ========================= 추천 피드 =========================

    @Transactional
    public RecommendFeedResponse getRecommendFeed(User user) {
        List<RoutineListResponse> hotRoutines = getHotRoutines(10);
        List<RoutineListResponse> personalRoutines = getPersonalRoutines(user, 10);
        TagPairSection tagPairSection1 = getTopTagPairSection(10);
        TagPairSection tagPairSection2 = getInterestTagPairSection(user, 10);
        return new RecommendFeedResponse(hotRoutines, personalRoutines, tagPairSection1, tagPairSection2);
    }

    @Transactional
    public List<RoutineListResponse> getHotRoutines(int limit) {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        double viewWeight = 0.5; // 기본값, 추후 외부에서 주입 가능
        double likeWeight = 0.5; // 기본값, 추후 외부에서 주입 가능
        return routineRepository.findHotRoutines(weekAgo, viewWeight, likeWeight, PageRequest.of(0, limit)).stream()
                .map(this::toRoutineListResponse)
                .toList();
    }

    @Transactional
    public List<RoutineListResponse> getPersonalRoutines(User user, int limit) {
        // 추천 루틴 중복 방지를 위한 Set
        Set<UUID> routineIds = new HashSet<>();
        List<RoutineListResponse> result = new ArrayList<>();

        // 1. 관심 태그 + 내 루틴 태그 기반 추천
        List<String> tagNames = new ArrayList<>();
        tagNames.addAll(getFavoriteTagNames(user)); // 관심 태그
        tagNames.addAll(getMyRoutineTagNames(user)); // 내 루틴 태그

        List<String> sortedTags = sortTagsByCount(tagNames);
        if (!sortedTags.isEmpty()) {
            List<Routine> routines = routineRepository.findRoutinesByTagsOrderByTagCount(sortedTags, PageRequest.of(0, limit));
            for (Routine routine : routines) {
                // Set으로 중복 방지
                if (routineIds.add(routine.getId())) {
                    result.add(toRoutineListResponse(routine));
                    if (result.size() >= limit) break;
                }
            }
        }

        // 2. scrap 루틴 태그 기반 추천 (위에서 limit 못 채웠을 때)
        if (result.size() < limit) {
            List<String> scrapTagNames = getScrapRoutineTagNames(user); // scrap 루틴 태그
            List<String> sortedScrapTags = sortTagsByCount(scrapTagNames);
            if (!sortedScrapTags.isEmpty()) {
                List<Routine> routines = routineRepository.findRoutinesByTagsOrderByTagCount(sortedScrapTags, PageRequest.of(0, limit));
                for (Routine routine : routines) {
                    // Set으로 중복 방지
                    if (routineIds.add(routine.getId())) {
                        result.add(toRoutineListResponse(routine));
                        if (result.size() >= limit) break;
                    }
                }
            }
        }

        // 3. 인기 루틴으로 채우기 (위에서 limit 못 채웠을 때)
        if (result.size() < limit) {
            List<RoutineListResponse> hotRoutines = getHotRoutines(limit);
            for (RoutineListResponse routine : hotRoutines) {
                // Set으로 중복 방지
                if (routineIds.add(routine.id())) {
                    result.add(routine);
                    if (result.size() >= limit) break;
                }
            }
        }

        // 최종적으로 limit 개수만큼 반환
        return result.stream().limit(limit).toList();
    }

    @Transactional
    public TagPairSection getTopTagPairSection(int limit) {
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        if (topPairs.isEmpty()) return null;
        for (TagPairCount pair : topPairs) {
            TagPairSection section = buildTagPairSection(pair, limit);
            if (section != null) return section;
        }
        return null;
    }

    @Transactional
    public TagPairSection getInterestTagPairSection(User user, int limit) {
        Set<String> interestTagIds = getFavoriteTagIds(user);
        List<TagPairCount> topPairs = routineTagRepository.findTopTagPairs();
        for (TagPairCount pair : topPairs) {
            if (interestTagIds.contains(pair.getTag1()) || interestTagIds.contains(pair.getTag2())) {
                TagPairSection section = buildTagPairSection(pair, limit);
                if (section != null) return section;
            }
        }
        return null;
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

    // ========================= 유틸/헬퍼 =========================

    private List<String> getFavoriteTagNames(User user) {
        return userFavoriteTagRepository.findAllByUserId(user.getId()).stream()
                .map(f -> f.getTag().getName()).toList();
    }

    private Set<String> getFavoriteTagIds(User user) {
        return userFavoriteTagRepository.findAllByUserId(user.getId()).stream()
                .map(f -> f.getTag().getId().toString()).collect(Collectors.toSet());
    }

    private List<String> getMyRoutineTagNames(User user) {
        List<Routine> myRoutines = routineRepository.findAllByUser(user);
        List<String> tagNames = new ArrayList<>();
        for (Routine r : myRoutines) {
            tagNames.addAll(routineTagRepository.findByRoutine(r).stream()
                    .map(rt -> rt.getTag().getName()).toList());
        }
        return tagNames;
    }

    private List<String> getScrapRoutineTagNames(User user) {
        List<RoutineUserAction> scraps = routineUserActionRepository.findAllByUserIdAndActionType(user.getId(), ActionType.SCRAP);
        List<String> tagNames = new ArrayList<>();
        for (RoutineUserAction scrap : scraps) {
            tagNames.addAll(routineTagRepository.findByRoutine(scrap.getRoutine()).stream()
                    .map(rt -> rt.getTag().getName()).toList());
        }
        return tagNames;
    }

    private List<String> sortTagsByCount(List<String> tagNames) {
        Map<String, Long> tagCount = tagNames.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        return tagCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .toList();
    }

    private TagPairSection buildTagPairSection(TagPairCount pair, int limit) {
        if (pair == null || pair.getTag1() == null || pair.getTag2() == null) {
            return null;
        }
        try {
            UUID tagId1 = UUID.fromString(pair.getTag1());
            UUID tagId2 = UUID.fromString(pair.getTag2());
            String tagName1 = tagRepository.findById(tagId1).map(Tag::getName).orElse("");
            String tagName2 = tagRepository.findById(tagId2).map(Tag::getName).orElse("");
            List<Routine> routines = routineRepository.findRoutinesByTagPair(tagId1, tagId2, PageRequest.of(0, limit));
            List<RoutineListResponse> routineList = routines.stream().map(this::toRoutineListResponse).toList();
            return new TagPairSection(tagName1, tagName2, routineList);
        } catch (IllegalArgumentException e) {
            // UUID 변환 실패 시 null 반환 (로그 남겨도 됨)
            return null;
        }
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

    private void saveRoutineTags(Routine routine, List<String> tagNames) {
        List<RoutineTag> routineTags = tagNames.stream()
                .map(tagName -> {
                    Tag tag = tagRepository.findByName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
                    return RoutineTag.builder().routine(routine).tag(tag).build();
                }).toList();
        routineTagRepository.saveAll(routineTags);
    }

    private void saveRoutineSteps(Routine routine, List<RoutineStepRequest> steps, boolean isSimple) {
        List<RoutineStep> routineSteps = steps.stream()
                .map(stepReq -> {
                    RoutineStep.RoutineStepBuilder builder = RoutineStep.builder()
                            .routine(routine)
                            .name(stepReq.name())
                            .stepOrder(stepReq.stepOrder());
                    if (!isSimple && stepReq.estimatedTime() != null) {
                        builder.estimatedTime(stepReq.estimatedTime());
                    }
                    return builder.build();
                }).toList();
        routineStepRepository.saveAll(routineSteps);
    }

    private void saveRoutineApps(Routine routine, List<String> selectedApps) {
        if (selectedApps == null || selectedApps.isEmpty()) return;
        List<RoutineApp> routineApps = selectedApps.stream()
                .map(pkg -> {
                    App app = appRepository.findByPackageName(pkg)
                            .orElseGet(() -> appRepository.save(
                                    App.builder().packageName(pkg).name(pkg).build()
                            ));
                    return RoutineApp.builder().routine(routine).app(app).build();
                }).toList();
        routineAppRepository.saveAll(routineApps);
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
        saveRoutineTags(routine, tags);
    }

    private void updateSteps(Routine routine, List<RoutineStepRequest> steps) {
        routineStepRepository.deleteByRoutine(routine);
        saveRoutineSteps(routine, steps, routine.isSimple());
    }

    private void updateApps(Routine routine, List<String> selectedApps) {
        routineAppRepository.deleteByRoutine(routine);
        saveRoutineApps(routine, selectedApps);
    }
}
