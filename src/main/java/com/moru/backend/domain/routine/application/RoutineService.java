package com.moru.backend.domain.routine.application;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

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
        // 루틴 엔티티 생성 및 저장 
        Routine routine = Routine.builder()
            .user(user)
            .title(request.getTitle())
            .isSimple(request.getIsSimple())
            .isUserVisible(request.getIsUserVisible())
            .likeCount(0)
            .content(Optional.ofNullable(request.getDescription()).orElse("")) // nullable 가능 
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
        // 요청에서 받은 루틴 단계들을 돌면서 이름, 순서, 예상 시간 정보를 갖는 RoutineStep 생성 
                .map(stepReq -> RoutineStep.builder()
                        .routine(savedRoutine)
                        .name(stepReq.getName())
                        .stepOrder(stepReq.getStepOrder())
                        .estimatedTime(LocalTime.parse(stepReq.getEstimatedTime()))
                        .build())
                .toList();
                // 저장 후 루틴과 연결
        routineStepRepository.saveAll(routineSteps);

        // 앱 저장 (최대 4개)
        List<RoutineApp> routineApps = request.getAppIds() == null ? List.of() :
                request.getAppIds().stream()
                        .map(appId -> {
                            //앱 ID 리스트가 있으면 루프를 돌면서: 
                            App app = appRepository.findById(appId)
                            // 해당 앱을 찾고 없으면 예외 발생
                                    .orElseThrow(() -> new IllegalArgumentException("앱을 찾을 수 없습니다: " + appId));
                            // 앱과 루틴을 연결하는 RoutineApp 객체를 생성해 저장 
                            return RoutineApp.builder()
                                    .routine(savedRoutine)
                                    .app(app)
                                    .build();
                        })
                        .toList();
        routineAppRepository.saveAll(routineApps);

        // isSimple에 따라 다른 응답 반환
        if (request.getIsSimple()) {
            return SimpleRoutineResponse.of(savedRoutine, routineTags);
        } else {
            return DetailedRoutineResponse.of(savedRoutine, routineTags, routineSteps, routineApps);
        }
    }
}
