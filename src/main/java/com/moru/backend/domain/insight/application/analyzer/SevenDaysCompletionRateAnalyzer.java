package com.moru.backend.domain.insight.application.analyzer;

import com.moru.backend.domain.insight.dto.CompletionRateResult;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SevenDaysCompletionRateAnalyzer implements CompletionRateAnalyzer {

    private final UserRepository userRepository;

    @Override
    public CompletionRateResult analyze(UUID userId) {
        CompletionRateResult completionRateResult = new CompletionRateResult();

        // 실천률 계산 기준 기간: 최근 7일
        LocalDate today = LocalDate.now();
        LocalDate from = today.minusDays(6);

        // 본인 실천률 계산
        double userRate = calculateUserRate(userId, from, today);
        completionRateResult.setUserCompletionRate(userRate);

        // 전체 유저 실천률 평균 + 분포
        List<User> allUser = userRepository.findAll();
        List<Double> allRates = new ArrayList<>();

        Map<Integer, Integer> distributionMap = new TreeMap<>();

        for(User user : allUser) {
            double rate = calculateUserRate(user.getId(), from, today);
            allRates.add(rate);

            // 실천률을 10단위 구간으로 정규화
            int bucket = ((int) rate / 10) * 10;

            //해당 구간(bucket)에 속한 사용자 수를 1 증가
            distributionMap.put(bucket, distributionMap.getOrDefault(bucket, 0) + 1);
        }

        // 전체 사용자의 평균 실천률을 계산
        completionRateResult.setGlobalAverageCompletionRate(
                allRates.stream().mapToDouble(d -> d).average().orElse(0.0) //
        );
        completionRateResult.setDistributionMap(distributionMap);

        return completionRateResult;
    }

    private double calculateUserRate(UUID userId, LocalDate from, LocalDate to) {
        // Repository에서 날짜별 설정된 루틴 개수 및 실천한 루틴 개수 조회 필요

    }
}
