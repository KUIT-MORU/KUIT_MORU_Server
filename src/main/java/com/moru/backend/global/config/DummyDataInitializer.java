package com.moru.backend.global.config;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.social.dao.UserFollowRepository;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("dev") // 개발 환경에서만 더미 데이터 생성하도록
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {
    // 직접 호출할 서비스 클래스
    private final DummyDataGenerator dummyDataGenerator;

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
        log.info("[1/5] {}개의 태그를 생성함", allTags.size());

        List<App> allApps = dummyDataGenerator.createManualApps();
        log.info("[2/5] {}개의 앱을 생성함", allApps.size());

        List<User> allUsers = dummyDataGenerator.createBulkUsers(10000);
        log.info("[3/5] {}명의 사용자를 생성했습니다.", allUsers.size());

        List<Routine> allRoutines = dummyDataGenerator.createBulkRoutines(20000, allUsers, allTags, allApps);
        log.info("[4/5] {}개의 루틴과 관련 데이터(스텝, 태그연결, 스케줄)를 생성했습니다.", allRoutines.size());

        dummyDataGenerator.createBulkRelationsAndLogs(50000, allUsers, allTags, allRoutines);
        log.info("[5/5] 팔로우, 선호 태그, 루틴 로그 데이터를 생성했습니다.");

        log.info("===더미 데이터 생성 완료===");
    }
}
