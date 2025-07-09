package com.moru.backend.global.validator;


import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.dao.RoutineAppRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.dto.request.AppInfo;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RoutineAppValidator {
    private final RoutineAppRepository routineAppRepository;
    private final AppRepository appRepository;

    /**
     * 여러 개 앱의 중복, 존재 여부를 한 번에 검증하고 앱 엔티티 리스트 반환
     * @param routine 루틴 엔티티
     * @param appInfos 앱 정보 리스트
     * @return 검증된 앱 엔티티 리스트
     */
    public List<App> validateBatchAppConnect(Routine routine, List<AppInfo> appInfos) {
        if (appInfos == null || appInfos.isEmpty()) {
            throw new CustomException(ErrorCode.APP_NOT_FOUND);
        }

        // 패키지명 중복 체크
        Set<String> uniquePackageNames = new HashSet<>();
        for (AppInfo appInfo : appInfos) {
            if (!uniquePackageNames.add(appInfo.packageName())) {
                throw new CustomException(ErrorCode.ALREADY_CONNECTED_APP);
            }
        }

        // 앱 존재 여부 및 중복 연결 체크
        List<App> apps = appInfos.stream().map(appInfo -> {
            // 패키지명으로 기존 앱 검색
            App app = appRepository.findByPackageName(appInfo.packageName())
                    // 없으면 새로 생성하기 & 있으면 기존 앱 생성
                    .orElseGet(() -> appRepository.save(
                            App.builder()
                                    .name(appInfo.appName())
                                    .packageName(appInfo.packageName())
                                    .build()
                    ));

            if (routineAppRepository.existsByRoutineAndApp_Id(routine, app.getId())) {
                throw new CustomException(ErrorCode.ALREADY_CONNECTED_APP);
            }

            return app;
        }).toList();

        return apps;
    }

    /**
     * 루틴에 연결된 앱이 존재하는지 검증
     * @param routine 루틴 엔티티
     * @param appId 앱 ID
     * @return 검증된 RoutineApp 엔티티
     */
    public RoutineApp validateRoutineAppExists(Routine routine, UUID appId) {
        return routineAppRepository.findByRoutineAndApp_Id(routine, appId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_APP_NOT_FOUND));
    }
}
