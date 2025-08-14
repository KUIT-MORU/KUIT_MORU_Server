package com.moru.backend.global.dummydata;

import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.dummydata.seeder.RoutineLikeSeeder;
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
    // 직접 호출할 서비스 클래스
    private final DummyDataGenerator dummyDataGenerator;
    private final DummyDataProperties dummyDataProperties;
    private final RoutineLikeSeeder routineLikeSeeder;

    @Override
    public void run(String... args) throws Exception {
        // --- 기존 generate() 메서드의 내용을 여기로 옮겨옵니다. ---
        if (dummyDataGenerator.isDataPresent()) {
            log.info("더미 데이터가 이미 존재하므로 생성 건너뛰기");
            return;
        }
        log.info("===DataFaker을 사용해서 대규모 더미 데이터 생성 시작===");

        // 각 단계가 별도의 트랜잭션으로 실행됩니다.
        List<Tag> allTags = dummyDataGenerator.createManualTags();
        log.info("[1/9] {}개의 태그를 생성함", allTags.size());

        List<App> allApps = dummyDataGenerator.createManualApps();
        log.info("[2/9] {}개의 앱을 생성함", allApps.size());

        List<User> allUsers = dummyDataGenerator.createBulkUsers(dummyDataProperties.getUserCount());
        log.info("[3/9] {}명의 사용자를 생성했습니다.", allUsers.size());

        List<Routine> allRoutines = dummyDataGenerator.createBulkRoutines(dummyDataProperties.getRoutineCount(), allUsers, allTags, allApps);
        routineLikeSeeder.seedLikesForRoutines(allRoutines, allUsers);

        log.info("[4/9] {}개의 루틴과 관련 데이터(스텝, 태그연결, 스케줄)를 생성했습니다.", allRoutines.size());

//        dummyDataGenerator.createBulkRelationsAndLogs(dummyDataProperties.getRelationCount(), allUsers, allTags, allRoutines);
//        log.info("[5/5] 팔로우, 선호 태그, 루틴 로그 데이터를 생성했습니다.");
        List<UserFollow> allFollows = dummyDataGenerator.createFollowRelations(dummyDataProperties.getFollowCount(), allUsers);
        log.info("[5/9] 팔로우 관계를 생성했습니다.");

        dummyDataGenerator.createFavoriteTagRelations(dummyDataProperties.getFavoriteTagCount(), allUsers, allTags);
        log.info("[6/9] 선호 태그 관계를 생성했습니다.");

        // 스크랩 액션 생성 추가
        dummyDataGenerator.createScrapActions(dummyDataProperties.getScrapCount(), allUsers, allRoutines);
        log.info("[7/9] 스크랩 관계를 생성했습니다.");

        dummyDataGenerator.createLogs(allRoutines, allUsers);
        log.info("[8/9] 루틴 로그를 생성했습니다.");

        dummyDataGenerator.createBulkNotifications(allFollows, allRoutines);
        log.info("[9/9] 알림 데이터를 생성했습니다.");

        log.info("===더미 데이터 생성 완료===");

        // 1) targetUserEmail로 기준 사용자 선택
        User target = allUsers.stream()
                .filter(u -> Optional.ofNullable(dummyDataProperties.getTargetUserEmail())
                        .map(e -> e.equalsIgnoreCase(u.getEmail()))
                        .orElse("test@example.com".equalsIgnoreCase(u.getEmail())))
                .findFirst()
                .orElse(allUsers.get(0));

        // 2) 기준 사용자 수신함 알림 보장
        dummyDataGenerator.createUserCentricNotifications(
                target,
                allFollows,
                allRoutines,
                Math.max(0, dummyDataProperties.getUserCentricFollowNotif()),
                Math.max(0, dummyDataProperties.getUserCentricRoutineCreatedNotif())
        );
        log.info("[추가] 사용자 기준 알림 생성 완료 (target: {})", target.getEmail());

        // 3) 검색기록 더미 (원하면 전체 유저, 아니면 target만)
        dummyDataGenerator.createSearchHistoriesPerUser(
                List.of(target), // 또는 allUsers
                Math.max(0, dummyDataProperties.getSearchHistoryPerUser())
        );
        log.info("[추가] 검색기록 더미 생성 완료");
    }
}
