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
import com.moru.backend.domain.routine.dto.response.RoutineCreateResponse;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.RoutineValidator;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.dto.request.RoutineUpdateRequest;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
    private final com.moru.backend.domain.social.application.ScrapService scrapService;
    private final com.moru.backend.domain.social.application.LikeService likeService;

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
