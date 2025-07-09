package com.moru.backend.domain.log.application;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineAppSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoutineLogService {
    private final RoutineRepository routineRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;

    public UUID startRoutine(User user, UUID routineId) {
        // 루틴 로드 (스텝, 태그, 앱을 포함하였음)
        Routine routine = routineRepository.findByRoutineIdWithStepsTagsApps(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

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
}
