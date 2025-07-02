package com.moru.backend.domain.routine.application;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.moru.backend.domain.Tag.dao.TagRepository;
import com.moru.backend.domain.Tag.domain.Tag;
import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineApp;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.RoutineTag;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.SimpleRoutineResponse;
import com.moru.backend.domain.routine.dto.response.DetailedRoutineResponse;
import com.moru.backend.domain.user.domain.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutineService {
    private final RoutineRepository routineRepository;
    private final RoutineStepRepository routineStepRepository;
    private final RoutineTagRepository routineTagRepository;
    private final RoutineAppRepository routineAppRepository;
    private final TagRepository tagRepository;
    private final AppRepository appRepository;

    public Object createRoutine(RoutineCreateRequest request, User user) {

        // 단순 루틴인지 확인 
        boolean isSimple = request.getIsSimple();
        LocalTime totalTime = null;
        if (!isSimple) {
            totalTime = request.getSteps().stream()
                .map(step -> step.getEstimatedTime() != null ? LocalTime.parse(step.getEstimatedTime()) : LocalTime.of(0, 0))
                .reduce(LocalTime.of(0, 0), (time1, time2) -> 
                    time1.plusHours(time2.getHour())
                        .plusMinutes(time2.getMinute())
                        .plusSeconds(time2.getSecond())
                );
        }
        
        // 루틴 엔티티 생성 및 저장 
        Routine routine = Routine.builder()
            .id(UUID.randomUUID())
            .user(user)
            .title(request.getTitle())
            .isSimple(isSimple)
            .isUserVisible(request.getIsUserVisible())
            .likeCount(0)
            .content(Optional.ofNullable(request.getDescription()).orElse("")) // nullable 가능 
            .requiredTime(isSimple ? null : totalTime)
            .status(true)
            .build();

        Routine savedRoutine = routineRepository.save(routine);

        // 태그 저장 (최대 3개)
        List<RoutineTag> routineTags = request.getTags().stream()
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
        List<RoutineStep> routineSteps = request.getSteps().stream()
            .map(stepReq -> {
                RoutineStep.RoutineStepBuilder builder = RoutineStep.builder()
                        .routine(savedRoutine)
                        .name(stepReq.getName())
                        .stepOrder(stepReq.getStepOrder());
                if (!isSimple && stepReq.getEstimatedTime() != null) {
                    builder.estimatedTime(LocalTime.parse(stepReq.getEstimatedTime()));
                }
                return builder.build();
            })
            .toList();
        routineStepRepository.saveAll(routineSteps);

        // 앱 저장 (최대 4개)
        List<RoutineApp> routineApps = List.of();
        if (!isSimple && request.getAppIds() != null) {
            routineApps = request.getAppIds().stream()
                    .map(appId -> {
                        App app = appRepository.findById(appId)
                                .orElseThrow(() -> new IllegalArgumentException("앱을 찾을 수 없습니다: " + appId));
                        return RoutineApp.builder()
                                .routine(savedRoutine)
                                .app(app)
                                .build();
                    })
                    .toList();
            routineAppRepository.saveAll(routineApps);
        }

        if (isSimple) {
            return SimpleRoutineResponse.of(savedRoutine, routineTags, routineSteps);
        } else {
            return DetailedRoutineResponse.of(savedRoutine, routineTags, routineSteps, routineApps);
        }
    }
}
