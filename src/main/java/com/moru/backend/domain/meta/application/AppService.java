package com.moru.backend.domain.meta.application;

import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.dto.AppResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 읽기 전용 트랜잭션
public class AppService {
    private final AppRepository appRepository;

    // todo : 사용자가 설치한 앱들을 반환하도록 만들기
    /**
     * 모든 앱 목록들을 조회합니다.
     */
    public List<AppResponse> getAllApps() {
        List<App> apps = appRepository.findAllOrderByName();
        return apps.stream()
                .map(AppResponse::from)
                .collect(Collectors.toList());
    }
}
