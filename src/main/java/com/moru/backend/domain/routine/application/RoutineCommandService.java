package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.notification.event.RoutineCreatedEvent;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.request.RoutineStepRequest;
import com.moru.backend.domain.routine.dto.request.RoutineUpdateRequest;
import com.moru.backend.domain.routine.dto.response.RoutineCreateResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.fcm.RoutineScheduleFcmPreloader;
import com.moru.backend.global.util.S3Directory;
import com.moru.backend.global.util.S3Service;
import com.moru.backend.global.validator.RoutineValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class RoutineCommandService {
    private final RoutineRepository routineRepository;
    private final TagRepository tagRepository;
    private final AppRepository appRepository;
    private final RoutineValidator routineValidator;
    private final S3Service s3Service;
    private final RoutineScheduleFcmPreloader routineScheduleFcmPreloader;
    private final ApplicationEventPublisher eventPublisher;

    public RoutineCreateResponse createRoutine(RoutineCreateRequest request, User user) {
        boolean isSimple = request.isSimple();
        Duration totalTime = isSimple ? null : request.steps().stream()
                .map(step -> Optional.ofNullable(step.estimatedTime()).orElse(Duration.ZERO))
                .reduce(Duration.ZERO, Duration::plus);

        // === 이미지 이동 처리 ===
        String imageKey = processImageKey(request.imageKey());

        Routine routine = Routine.builder()
                .user(user)
                .title(request.title())
                .isSimple(isSimple)
                .isUserVisible(request.isUserVisible())
                .likeCount(0)
                .content(Optional.ofNullable(request.description()).orElse(""))
                .requiredTime(totalTime)
                .status(true)
                .imageUrl(imageKey)
                .build();

        updateTags(routine, request.tags());
        updateSteps(routine, request.steps());
        updateApps(routine, request.selectedApps());
        Routine savedRoutine = routineRepository.save(routine);

        // 공개 루틴인 경우에만 알림 이벤트 발행
        if(routine.isUserVisible()) {
            eventPublisher.publishEvent(
                    RoutineCreatedEvent.builder()
                            .routineId(savedRoutine.getId())
                            .senderId(user.getId())
                            .build()
            );
        }

        // 스케줄 알림 설정하기
        routineScheduleFcmPreloader.preloadRoutineScheduleFcm(savedRoutine);

        return new RoutineCreateResponse(savedRoutine.getId(), savedRoutine.getTitle(), savedRoutine.getCreatedAt());
    }

    public void updateRoutine(UUID routineId, RoutineUpdateRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);

        updateSimpleFields(routine, request);
        if (request.tags() != null) updateTags(routine, request.tags());
        if (request.steps() != null) updateSteps(routine, request.steps());
        if (request.selectedApps() != null) updateApps(routine, request.selectedApps());

        // 루틴 수정에 따른 푸시 알림 예약 갱신
        routineScheduleFcmPreloader.refreshRoutineScheduleFcm(routine);
    }

    public void deleteRoutine(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        // 루틴 삭제에 따른 푸시 알림 예약 삭제
        routineScheduleFcmPreloader.removeRoutineScheduleFcm(routine);

        routineRepository.delete(routine);
    }

    private void updateSimpleFields(Routine routine, RoutineUpdateRequest request) {
        if (request.title() != null) routine.setTitle(request.title());
        if (request.description() != null) routine.setContent(request.description());

        // === 이미지 이동 처리 ===
        String imageKey = processImageKey(request.imageUrl());

        routine.setImageUrl(imageKey);

        if (request.isUserVisible() != null) routine.setUserVisible(request.isUserVisible());
        if (request.isSimple() != null) routine.setSimple(request.isSimple());
    }

    private void updateTags(Routine routine, List<String> tagNames) {
        // JPA의 변경 감지(Dirty Checking)와 orphanRemoval=true를 활용하기 위해 clear 후 add
        routine.getRoutineTags().clear();
        tagNames.stream()
                .map(tagName -> tagRepository.findByName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build())))
                .forEach(tag -> routine.addRoutineTag(RoutineTag.builder().tag(tag).build()));
    }

    private void updateSteps(Routine routine, List<RoutineStepRequest> steps) {
        routine.getRoutineSteps().clear();
        steps.forEach(stepReq -> {
            RoutineStep.RoutineStepBuilder builder = RoutineStep.builder()
                    .name(stepReq.name())
                    .stepOrder(stepReq.stepOrder());
            if (!routine.isSimple() && stepReq.estimatedTime() != null) {
                builder.estimatedTime(stepReq.estimatedTime());
            }
            routine.addRoutineStep(builder.build());
        });
    }

    private void updateApps(Routine routine, List<String> selectedApps) {
        routine.getRoutineApps().clear();
        if (selectedApps == null || selectedApps.isEmpty()) return;

        selectedApps.stream()
                .map(pkg -> appRepository.findByPackageName(pkg)
                        .orElseGet(() -> appRepository.save(App.builder().packageName(pkg).name(pkg).build())))
                .forEach(app -> routine.addRoutineApp(RoutineApp.builder().app(app).build()));
    }

    @Nullable
    private String processImageKey(String imageKey) {
        if (imageKey != null && !imageKey.isBlank()) {
            return s3Service.moveToRealLocation(imageKey, S3Directory.ROUTINE);
        }
        return null;
    }
}
