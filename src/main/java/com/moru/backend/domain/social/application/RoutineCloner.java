package com.moru.backend.domain.social.application;

import com.moru.backend.domain.routine.dao.*;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.util.S3Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoutineCloner {
    private final RoutineRepository routineRepository;
    private final RoutineStepRepository routineStepRepository;
    private final RoutineTagRepository routineTagRepository;
    private final RoutineAppRepository routineAppRepository;
    private final RoutineScheduleRepository routineScheduleRepository;
    private final S3Service s3Service;

    @Transactional
    public Routine cloneRoutine(Routine origin, User owner) {
        // 루틴 복제
        Routine copy = Routine.builder()
                .title(origin.getTitle())
                .content(origin.getContent())
                .isSimple(origin.isSimple())
                .isUserVisible(origin.isUserVisible())
                .requiredTime(origin.getRequiredTime())
                .imageUrl(origin.getImageUrl())
                .user(owner)
                .build();
        routineRepository.save(copy);

        // 스텝 복제
        for (RoutineStep step : origin.getRoutineSteps()) {
            RoutineStep newStep = RoutineStep.builder()
                    .routine(copy)
                    .name(step.getName())
                    .estimatedTime(step.getEstimatedTime())
                    .stepOrder(step.getStepOrder())
                    .build();
            routineStepRepository.save(newStep);
        }

        // 태그 복제
        for(RoutineTag rt : origin.getRoutineTags()) {
            RoutineTag newRt = RoutineTag.builder()
                    .routine(copy)
                    .tag(rt.getTag())
                    .build();
            routineTagRepository.save(newRt);
        }

        // 앱 복제
        for(RoutineApp ra : origin.getRoutineApps()) {
            RoutineApp newRa = RoutineApp.builder()
                    .routine(copy)
                    .app(ra.getApp())
                    .build();
            routineAppRepository.save(newRa);
        }

        // 스케줄 복제
        List<RoutineSchedule> schedules = routineScheduleRepository
                .findAllByRoutineId(origin.getId());
        for (RoutineSchedule rs : schedules) {
            RoutineSchedule newRs = RoutineSchedule.builder()
                    .routine(copy)
                    .dayOfWeek(rs.getDayOfWeek())
                    .time(rs.getTime())
                    .alarmEnabled(rs.isAlarmEnabled())
                    .build();
            routineScheduleRepository.save(newRs);
        }

        return copy;
    }
}
