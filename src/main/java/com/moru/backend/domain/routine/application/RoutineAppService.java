package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.dto.request.RoutineAppRequest;
import com.moru.backend.domain.routine.dto.response.RoutineAppResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.validator.RoutineAppValidator;
import com.moru.backend.global.validator.RoutineValidator;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineAppService {

    private final RoutineValidator routineValidator;
    private final RoutineAppValidator routineAppValidator;
    private final RoutineAppRepository routineAppRepository;

    @Transactional
    public List<RoutineAppResponse> connectAppToRoutine(UUID routineId, RoutineAppRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);

        List<App> apps = routineAppValidator.validateBatchAppConnect(routine, request.apps());
        for (App app : apps) {
            RoutineApp routineApp = RoutineApp.builder()
                    .routine(routine)
                    .app(app)
                    .build();
            routineAppRepository.save(routineApp);
        }

        List<RoutineApp> routineApps = routineAppRepository.findByRoutine(routine);
        return routineApps.stream()
                .map(routineApp -> RoutineAppResponse.from(
                        routineApp.getApp()))
                .collect(Collectors.toList());
    }

    public List<RoutineAppResponse> getRoutineApps(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        List<RoutineApp> routineApps = routineAppRepository.findByRoutine(routine);

        return routineApps.stream()
                .map(routineApp -> RoutineAppResponse.from(routineApp.getApp()))
                .collect(Collectors.toList());
    }
}
