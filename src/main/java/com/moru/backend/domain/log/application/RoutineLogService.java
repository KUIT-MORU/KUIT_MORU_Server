package com.moru.backend.domain.log.application;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.dao.RoutineStepLogRepository;
import com.moru.backend.domain.log.dao.RoutineStepSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.RoutineStepLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineAppSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.log.dto.RoutineLogDetailResponse;
import com.moru.backend.domain.log.dto.RoutineStepLogCreateRequest;
import com.moru.backend.domain.log.dto.RoutineStepLogDto;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.response.RoutineAppResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.validator.RoutineValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutineLogService {
    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;
    private final RoutineStepRepository routineStepRepository;
    private final RoutineValidator routineValidator;
    private final RoutineStepSnapshotRepository routineStepSnapshotRepository;
    private final RoutineStepLogRepository routineStepLogRepository;

    public UUID startRoutine(User user, UUID routineId) {
        // 루틴 권한 검증 및 조회
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, user);

        // 스냅샷 생성
        RoutineSnapshot snapshot = createSnapshotFromRoutine(routine);

        // 스냅샷 저장
        routineSnapshotRepository.save(snapshot); // cascade로 모두 저장됨

        // 루틴 로그 생성
        RoutineLog routineLog = RoutineLog.builder()
                .user(user)
                .routineSnapshot(snapshot)
                .startedAt(LocalDateTime.now())
                .isSimple(snapshot.isSimple())
                .isCompleted(false)
                .build();

        routineLogRepository.save(routineLog);
        return routineLog.getId();
    }

    private RoutineSnapshot createSnapshotFromRoutine(Routine routine) {
        // 스냅샷 생성
        // 루틴 스냅샷 생성
        RoutineSnapshot snapshot = RoutineSnapshot.builder()
                .title(routine.getTitle())
                .content(routine.getContent())
                .imageUrl(routine.getImageUrl())
                .isSimple(routine.isSimple())
                .isUserVisible(routine.isUserVisible())
                .requiredTime(routine.getRequiredTime())
                .build();

        // 루틴 스탭 스냅샷 생성
        for(RoutineStep step : routine.getRoutineSteps()) {
            RoutineStepSnapshot stepSnapshot = RoutineStepSnapshot.builder()
                    .routineSnapshot(snapshot)
                    .stepOrder(step.getStepOrder())
                    .name(step.getName())
                    .estimatedTime(step.getEstimatedTime())
                    .build();
            snapshot.getStepSnapshots().add(stepSnapshot);
        }

        // 태그 스냅샷 생성
        for(RoutineTag tag : routine.getRoutineTags()) {
            RoutineTagSnapshot tagSnapshot = RoutineTagSnapshot.builder()
                    .routineSnapshot(snapshot)
                    .tagName(tag.getTag().getName())
                    .build();
            snapshot.getTagSnapshots().add(tagSnapshot);
        }

        // 앱 스냅샷 생성
        for(RoutineApp app : routine.getRoutineApps()) {
            RoutineAppSnapshot appSnapshot = RoutineAppSnapshot.builder()
                    .routineSnapshot(snapshot)
                    .name(app.getApp().getName())
                    .packageName(app.getApp().getPackageName())
                    .build();
            snapshot.getAppSnapshots().add(appSnapshot);
        }
        return snapshot;
    }

    public RoutineLogDetailResponse getRoutineLogDetail(User user, UUID routineLogId) {
        // 루틴 로그 조회
        RoutineLog routineLog = routineLogRepository.findByRoutineLogIdWithSnapshotAndSteps(routineLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_LOG_NOT_FOUND));

        // 접근 권한 확인
        if(!routineLog.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        RoutineSnapshot snapshot = routineLog.getRoutineSnapshot();

        List<String> tagNames = snapshot.getTagSnapshots().stream()
                .map(RoutineTagSnapshot::getTagName)
                .toList();

        List<RoutineAppResponse> apps = snapshot.getAppSnapshots().stream()
                .map(app -> new RoutineAppResponse(app.getPackageName(), app.getName()))
                .toList();

        List<RoutineStepLogDto> steps = routineLog.getRoutineStepLogs().stream()
                .map(stepLog -> {
                    RoutineStepSnapshot stepSnapshot = stepLog.getRoutineStep();
                    return new RoutineStepLogDto(
                            stepLog.getStepOrder(),
                            stepSnapshot.getName(),
                            stepLog.getNote(),
                            stepSnapshot.getEstimatedTime(),
                            stepLog.getActualTime()
                    );
                })
                .toList();

        return RoutineLogDetailResponse.from(routineLog, snapshot, tagNames, steps, apps);
    }

    @Transactional
    public void saveStepLog(UUID routineLogId, User user, RoutineStepLogCreateRequest request) {
        // 루틴 로그 조회
        RoutineLog log = routineLogRepository.findById(routineLogId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_LOG_NOT_FOUND));

        // 접근 권한 확인
        if(!log.getUser().getId().equals(user.getId())) {
            throw new  CustomException(ErrorCode.FORBIDDEN);
        }

        // 루틴 스텝 스냅샷 조회
        RoutineStepSnapshot stepSnapshot = routineStepSnapshotRepository
                .findByRoutineSnapshotIdAndStepOrder(log.getRoutineSnapshot().getId(), request.stepOrder())
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_STEP_SNAPSHOT_NOT_FOUND));

        RoutineStepLog stepLog = RoutineStepLog.builder()
                .routineLog(log)
                .routineStep(stepSnapshot)
                .stepOrder(request.stepOrder())
                .note(request.note())
                .actualTime(request.actualTime())
                .startedAt(request.startAt())
                .endedAt(request.endedAt())
                .pausedTime(request.pausedTime())
                .build();

        routineStepLogRepository.save(stepLog);
    }
}