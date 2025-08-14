package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.routine.dao.SearchHistoryRepository;
import com.moru.backend.domain.routine.domain.search.SearchHistory;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineSearchSeeder {
    private final SearchHistoryRepository searchHistoryRepository;
    private final Random random = new Random();
    private static final int BATCH_SIZE = 200; // 배치 크기를 상수로 관리

    @Transactional
    public void createSearchHistoriesPerUser(List<User> users, int perUser) {
        if (perUser <= 0 || users.isEmpty()) return;

        List<String> keywords = Arrays.asList("아침","운동","다이어트","명상","헬스","공부","프로그래밍","요가","산책","모닝루틴");

        // 프로젝트 실제 enum 기준으로 생성 (필요시 제외할 값 필터)
        List<SearchType> allowedTypes = Arrays.stream(SearchType.values())
                // .filter(t -> t != SearchType.ALL) // 필요하면 제외
                .collect(Collectors.toList());

        List<SearchHistory> batch = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (User u : users) {
            for (int i = 0; i < perUser; i++) {
                String kw = keywords.get(random.nextInt(keywords.size()));
                SearchType tp = allowedTypes.get(random.nextInt(allowedTypes.size()));

                batch.add(SearchHistory.builder()
                        .userId(u.getId())
                        .searchKeyword(kw)
                        .searchType(tp)                    // ← 엔티티가 enum
                        .createdAt(now.minusMinutes(random.nextInt(7 * 24 * 60)))
                        .build());

                if (batch.size() >= BATCH_SIZE) {
                    searchHistoryRepository.saveAll(batch);
                    batch.clear();
                }
            }
        }
        if (!batch.isEmpty()) searchHistoryRepository.saveAll(batch);
        log.info("검색기록 더미 생성 완료 (perUser: {})", perUser);
    }
}
