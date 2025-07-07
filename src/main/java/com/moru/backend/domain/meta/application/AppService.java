package com.moru.backend.domain.meta.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.dto.request.InstalledAppsRequest;
import com.moru.backend.domain.meta.dto.response.AppResponse;
import com.moru.backend.domain.meta.dto.response.InstalledAppResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class AppService {

    /**
     * 설치된 앱 목록을 처리하고 응답합니다.
     */
    public List<InstalledAppResponse> processInstalledApps(InstalledAppsRequest request) {
        return request.getInstalledApps().stream()
                .map(appRequest -> InstalledAppResponse.builder()
                        .id(null) // 동적으로 생성된 앱이므로 ID는 null
                        .name(appRequest.getAppName())
                        .packageName(appRequest.getPackageName())
                        .iconUrl(appRequest.getIconBase64())
                        .build())
                .collect(Collectors.toList());
    }
}
