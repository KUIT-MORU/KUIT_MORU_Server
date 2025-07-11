package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.RoutineCreateResponse;
import com.moru.backend.domain.routine.dto.response.RoutineDetailResponse;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.RoutineValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    private final TagRepository tagRepository;
    private final AppRepository appRepository;
    private final RoutineValidator routineValidator;

    @Transactional
    public RoutineCreateResponse createRoutine(RoutineCreateRequest request, User user) {
        // 검증용 로그 추가
        System.out.println("[DEBUG] Routine 생성 요청: user=" + user + ", userId=" + (user != null ? user.getId() : null));
        boolean isSimple = request.isSimple();
        Duration totalTime = null;
        if (!isSimple) {
            System.out.println("[DEBUG] 시간 계산 시작:");
            totalTime = request.steps().stream()
                .map(step -> {
                    Duration stepTime = step.estimatedTime() != null ? step.estimatedTime() : Duration.ZERO;
                    System.out.println("[DEBUG] 스텝: " + step.name() + ", 시간: " + stepTime);
                    return stepTime;
                })
                .reduce(Duration.ZERO, (acc, stepTime) -> {
                    Duration result = acc.plus(stepTime);
                    System.out.println("[DEBUG] 누적 시간: " + result);
                    return result;
                });
            System.out.println("[DEBUG] 최종 총 시간: " + totalTime);
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
    public List<RoutineListResponse> getRoutineList(User user) {
        List<Routine> routines = routineRepository.findAllByUser(user);
        return routines.stream()
        .map(routine -> {
            List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
            return RoutineListResponse.of(routine, tags);
        })
        .toList();
    }

    @Transactional
    public RoutineDetailResponse getRoutineDetail(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        List<RoutineTag> tags = routineTagRepository.findByRoutine(routine);
        List<RoutineStep> steps = routineStepRepository.findByRoutineOrderByStepOrder(routine);
        List<RoutineApp> apps = routineAppRepository.findByRoutine(routine);
        return RoutineDetailResponse.of(routine, tags, steps, apps);
    }

}
