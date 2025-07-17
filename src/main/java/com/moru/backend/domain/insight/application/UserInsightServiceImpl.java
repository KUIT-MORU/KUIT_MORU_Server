package com.moru.backend.domain.insight.application;

import com.moru.backend.domain.insight.domain.UserInsight;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserInsightServiceImpl implements UserInsightService {

    private final UserInsight

    @Override
    public void updateUserInsight(UUID userId) {
        // TODO: 각 Analyzer 호출 -> 결과 병합 -> UserInsight 저장
    }

    @Override
    public void updateAllUserInsights() {
        // TODO: 전체 유저 목록을 가져와 반복적으로 updateUserInsight 호출
    }

    @Override
    public UserInsight getUserInsight(UUID userId) {
        return userInsightRepository.findByUserId(userId)
                .orElseGet(() -> {
                    updateUserInsight(userId);
                    return userInsightRepository.findByUserId(userId).orElseThrow();
                })
    }
}
