package com.moru.backend.global.dummydata;

import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.global.dummydata.seeder.*;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
@Profile("init-db") // 개발 환경에서만 더미 데이터 생성하도록
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {
    // [REFACTOR] 각 Seeder를 주입받아 사용
    private final UserSeeder userSeeder;
    private final RoutineSeeder routineSeeder;
    private final SocialRelationSeeder socialRelationSeeder;
    private final RoutineLogSeeder routineLogSeeder;
    private final NotificationSeeder notificationSeeder;
    private final RoutineSearchSeeder routineSearchSeeder;
    private final DummyDataProperties dummyDataProperties;

    @Override
    public void run(String... args) throws Exception {
        if (userSeeder.isDataPresent()) {
            log.info("더미 데이터가 이미 존재하므로 생성 건너뛰기");
            return;
        }
        log.info("===DataFaker을 사용해서 대규모 더미 데이터 생성 시작===");

        // 1. 기본 엔티티 생성
        List<Tag> allTags = userSeeder.createManualTags();
        log.info("[1/10] {}개의 태그를 생성함", allTags.size());

        List<App> allApps = userSeeder.createManualApps();
        log.info("[2/10] {}개의 앱을 생성함", allApps.size());

        List<User> allUsers = userSeeder.createBulkUsers(dummyDataProperties.getUserCount());
        log.info("[3/10] {}명의 사용자를 생성했습니다.", allUsers.size());

        List<Routine> allRoutines = routineSeeder.createBulkRoutines(dummyDataProperties.getRoutineCount(), allUsers, allTags, allApps);
        log.info("[4/10] {}개의 루틴과 관련 데이터(스텝, 태그연결, 스케줄)를 생성했습니다.", allRoutines.size());

        // 2. 관계 및 액션 데이터 생성
        List<UserFollow> allFollows = socialRelationSeeder.createFollowRelations(dummyDataProperties.getFollowCount(), allUsers);
        log.info("[5/10] 팔로우 관계를 생성했습니다.");

        socialRelationSeeder.createFavoriteTagRelations(dummyDataProperties.getFavoriteTagCount(), allUsers, allTags);
        log.info("[6/10] 선호 태그 관계를 생성했습니다.");

        socialRelationSeeder.createScrapActions(dummyDataProperties.getScrapCount(), allUsers, allRoutines);
        log.info("[7/10] 스크랩 관계를 생성했습니다.");

        socialRelationSeeder.seedLikesAndUpdateCount(allRoutines, allUsers);
        log.info("[8/10] 좋아요 관계를 생성하고 루틴의 likeCount를 업데이트했습니다.");

        // 3. 활동 기록 데이터 생성
        routineLogSeeder.createLogs(allRoutines, allUsers);
        log.info("[9/10] 루틴 로그를 생성했습니다.");

        notificationSeeder.createBulkNotifications(allFollows, allRoutines);
        log.info("[10/10] 알림 데이터를 생성했습니다.");

        log.info("===더미 데이터 생성 완료===");

        // 4. 특정 사용자를 위한 추가 데이터 생성
        User target = allUsers.stream()
                .filter(u -> Optional.ofNullable(dummyDataProperties.getTargetUserEmail())
                        .map(e -> e.equalsIgnoreCase(u.getEmail()))
                        .orElse("test@example.com".equalsIgnoreCase(u.getEmail())))
                .findFirst()
                .orElse(allUsers.get(0));

        notificationSeeder.createUserCentricNotifications(
                target,
                allFollows,
                allRoutines,
                Math.max(0, dummyDataProperties.getUserCentricFollowNotif()),
                Math.max(0, dummyDataProperties.getUserCentricRoutineCreatedNotif())
        );

        routineSearchSeeder.createSearchHistoriesPerUser(
                List.of(target), // 또는 allUsers
                Math.max(0, dummyDataProperties.getSearchHistoryPerUser())
        );
        log.info("[추가] 검색기록 더미 생성 완료");
    }
}
