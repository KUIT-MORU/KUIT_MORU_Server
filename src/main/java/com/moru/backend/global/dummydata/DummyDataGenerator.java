package com.moru.backend.global.dummydata;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.routine.dao.RoutineScheduleHistoryRepository;
import com.moru.backend.domain.routine.dao.SearchHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.search.SearchHistory;
import com.moru.backend.domain.routine.domain.search.SearchType;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDataGenerator {
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;
    private final NotificationRepository notificationRepository;
    private final SearchHistoryRepository searchHistoryRepository;

    // Faker 인스턴스와 Random 객체를 필드로 선언해서 재사용하기
    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();
    private final RoutineScheduleHistoryRepository routineScheduleHistoryRepository;



    // 테스트용 공통 비밀번호 (실제 암호화된 값)
    private static final String COMMON_PASSWORD_HASH = "$2a$12$/OXNM8oYy5chh/iOUA3j3.XjIEYi9Zbg/kiVT3.D/.zP2cev/5EDq"; // 1234abcde!@
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
