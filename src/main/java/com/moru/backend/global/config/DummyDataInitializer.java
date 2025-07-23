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
@Transactional
public class DummyDataInitializer implements CommandLineRunner {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final RoutineRepository routineRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;

    // Faker 인스턴스와 Random 객체를 필드로 선언해서 재사용하기
    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();

    // 테스트용 공통 비밀번호 (실제 암호화된 값)
    private static final String COMMON_PASSWORD_HASH = "$2a$10$j5YhIig/vZwnhy1D61vdm.J9djNvHLjdZAx8xTccYpGabXA7S2MGi"; // password
    private static final int BATCH_SIZE = 1000; // 배치 크기를 상수로 관리

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("더미 데이터가 이미 존재하므로 생성 건너뛰기");
            return;
        }
        log.info("===DataFaker을 사용해서 대규모 더미 데이터 생성 시작===");

        // 수동 생성 : 태그, 앱와 같은 고정적인 데이터
        List<Tag> allTags = createManualTags();
        log.info("[1/5] {}개의 태그를 생성함", allTags.size());

        List<App> allApps = createManualApps();
        log.info("[2/5] {}개의 앱을 생성함", allApps.size());

        // 자동 생성 : 사용자, 루틴 등
        List<User> allUsers = createBulkUsers(10000); // 10000명의 사용자를 생성
        log.info("[3/5] {}명의 사용자를 생성했습니다.", allUsers.size());

        List<Routine> allRoutines = createBulkRoutines(20000, allUsers, allTags, allApps); // 약 2만개의 루틴 생성
        log.info("[4/5] {}개의 루틴과 관련 데이터(스텝, 태그연결, 스케줄)를 생성했습니다.", allRoutines.size());

        // 3. 관계 데이터 생성 (팔로우, 선호 태그, 루틴 로그)
        createBulkRelationsAndLogs(50000, allUsers, allTags, allRoutines);
        log.info("[5/5] 팔로우, 선호 태그, 루틴 로그 데이터를 생성했습니다.");

        log.info("===더미 데이터 생성 완료===");
    }

    private List<Tag> createManualTags() {
        Set<String> tagNames = new HashSet<>(Arrays.asList(
                // rou_tag
                "가족", "걷기", "계획", "공강", "공부", "귀가후", "글쓰기", "기록", "기분나쁠때", "기분좋을때",
                "다이어트", "달리기", "도서관", "독서", "드라이브", "등교길", "등교전", "등산", "맑은날", "면접",
                "명상", "모닝루틴", "미팅", "반려동물", "발표", "방정리", "방학", "버스안", "복습", "브이로그",
                "블로그", "비온날", "사진", "산책", "세탁", "쇼핑", "수능", "스케줄", "스터디", "스트레칭",
                "습관", "시험", "식단", "아침", "악기", "암기", "야근중", "업무", "여행", "연습",
                "영어", "예습", "외출전", "외출중", "요가", "요리", "운동", "운전", "음악", "일기",
                "일본어", "일어나서", "일정관리", "자격증", "자기전", "자료조사", "자유시간", "자전거", "작업", "장보기",
                "재테크", "저녁", "점심", "주말밤", "준비", "지하철", "집콕", "청소", "체조", "체중",
                "출근길", "출근전", "취준", "친구", "카페", "토익", "토플", "퇴근길", "퇴근후", "평일밤",
                "프로그래밍", "필사", "하교길", "하교후", "헬스", "혼자", "회의", "회화", "휴식", "휴일",
                // ob_rou_tag1
                "출근길", "지하철", "퇴근길", "모닝루틴", "일어나서", "저녁", "자기전", "휴일", "공강",
                // ob_rou_tag2
                "독서", "과제", "공부", "작업", "다이어트", "수능", "취준", "프로그래밍", "휴식"
        ));

        List<Tag> tagsToCreate = tagNames.stream()
                .map(name -> Tag.builder().name(name).build()) // <- ID 생성 코드 삭제
                .collect(Collectors.toList());
        return tagRepository.saveAll(tagsToCreate);
    }

    private List<App> createManualApps() {
        List<App> apps = Arrays.asList(
                App.builder().name("카카오톡").packageName("com.kakao.talk").build(),
                App.builder().name("인스타그램").packageName("com.instagram.android").build(),
                App.builder().name("유튜브").packageName("com.google.android.youtube").build(),
                App.builder().name("네이버").packageName("com.naver.app").build()
        );
        return appRepository.saveAll(apps);
    }

    private List<User> createBulkUsers(int count) {
        List<User> allGeneratedUsers = new ArrayList<>();
        List<User> userBatch = new ArrayList<>();
        // 1. 테스트용 고정 사용자 추가 (data.sql 내용 반영)
        User testUser = User.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .email("test@example.com")
                .password("$2a$10$xUBrBGPuFIPdnDU2LCRLOeb3ML.vcGEen7NrughMZcQAs/i4cbxsy")
                .nickname("테스트유저")
                .gender(Gender.MALE)
                .birthday(LocalDate.parse("2000-01-01"))
                .bio("테스트 계정입니다.")
                .profileImageUrl("https://example.com/profile0.jpg")
                // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                .build();
        userRepository.save(testUser);
        allGeneratedUsers.add(testUser);

        Set<String> generatedEmails = new HashSet<>();
        generatedEmails.add("test@example.com"); // 고정 사용자 이메일 추가
        Set<String> generatedNicknames = new HashSet<>();
        generatedNicknames.add("테스트유저"); // 고정 사용자 닉네임 추가

        for (int i = 0; i < count - 1; i++) {
            // --- DB 조회 없이 메모리에서만 닉네임 중복 확인 ---
            String nickname;
            do {
                nickname = faker.name().lastName() + faker.name().firstName();
                if (nickname.length() > 20) {
                    nickname = nickname.substring(0, 20);
                }
            } while (!generatedNicknames.add(nickname)); // Set.add()는 추가 성공 시 true, 실패(중복) 시 false 반환

            // --- DB 조회 없이 메모리에서만 이메일 중복 확인 ---
            String email;
            do {
                email = faker.internet().safeEmailAddress();
            } while (!generatedEmails.add(email));

            // 1. DataFaker로 구버전 Date 객체 생성
            java.util.Date birthdayAsDate = faker.date().birthday(18, 65);
            // 2. Date 객체를 안정적인 최신 LocalDate 객체로 변환
            LocalDate birthday = birthdayAsDate.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            userBatch.add(User.builder()
                    .id(UUID.randomUUID())
                    .email(email)
                    .password(COMMON_PASSWORD_HASH)
                    .nickname(nickname)
                    .gender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE)
                    .birthday(birthday)
                    .bio(faker.lorem().sentence())
                    // profileImageUrl은 faker로 직접 생성하여 설정
                    .profileImageUrl(faker.avatar().image())
                    // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                    .build());

            // BATCH_SIZE(1000개)가 모일 때마다 DB에 저장하고, 메모리를 비웁니다.
            if (userBatch.size() >= BATCH_SIZE) {
                List<User> savedUsers = userRepository.saveAll(userBatch);
                allGeneratedUsers.addAll(savedUsers);
                userBatch.clear();
                log.info("{}명의 사용자 중간 저장 완료...", allGeneratedUsers.size());
            }
        }
        // 루프가 끝난 후 남은 사용자들을 저장합니다.
        if (!userBatch.isEmpty()) {
            List<User> savedUsers = userRepository.saveAll(userBatch);
            allGeneratedUsers.addAll(savedUsers);
        }

        return allGeneratedUsers;
    }

    private List<Routine> createBulkRoutines(int count, List<User> users, List<Tag> tags, List<App> apps) {
        List<Routine> allGeneratedRoutines = new ArrayList<>();
        List<Routine> routineBatch = new ArrayList<>();

        for (int i=0; i<count; i++) {
            User owner = users.get(random.nextInt(users.size()));
            boolean isSimpleRoutine = random.nextBoolean(); // 단순/집중 루틴 랜덤 결정

            Routine routine = Routine.builder()
                    .id(UUID.randomUUID())
                    .user(owner)
                    .title(faker.lorem().characters(5, 10))
                    .content(String.join("\n", faker.lorem().paragraphs(2)))
                    .isSimple(isSimpleRoutine)
                    .isUserVisible(true)
                    .likeCount(random.nextInt(500))
                    .viewCount(random.nextInt(2000))
                    // requiredTime은 아래에서 스텝 시간 합계로 계산되므로 여기서는 설정하지 않음
                    .status(true)
                    .build();

            // --- 스텝 생성 및 시간 계산 로직 ---
            int stepCount = random.nextInt(5) + 1;
            List<RoutineStep> steps = new ArrayList<>();
            Duration totalRequiredTime = Duration.ZERO; // 총 소요시간 초기화

            for (int j = 1; j <= stepCount; j++) {
                RoutineStep.RoutineStepBuilder stepBuilder = RoutineStep.builder()
                        .routine(routine)
                        .name(faker.lorem().word() + "하기")
                        .stepOrder(j);

                // 집중 루틴(isSimple=false)일 경우에만 estimatedTime 설정
                if (!isSimpleRoutine) {
                    Duration estimatedTime = Duration.ofMinutes(random.nextInt(30) + 1);
                    stepBuilder.estimatedTime(estimatedTime);
                    totalRequiredTime = totalRequiredTime.plus(estimatedTime); // 스텝 시간을 총 시간에 더함
                }
                // 단순 루틴의 경우 estimatedTime은 null로 유지됨
                steps.add(stepBuilder.build());
            }
            routine.setRoutineSteps(steps);

            // 계산된 총 소요 시간을 루틴에 설정 (집중 루틴만)
            if (!isSimpleRoutine) {
                routine.setRequiredTime(totalRequiredTime);
            }
            // --- 로직 종료 ---

            // 집중 루틴(isSimple=false)일 경우에만 앱 연결
            if (!isSimpleRoutine && !apps.isEmpty()) {
                Collections.shuffle(apps);
                int appCount = random.nextInt(2) + 1; // 1~2개 앱 연결
                List<RoutineApp> routineApps = new ArrayList<>();
                for (int j = 0; j < appCount; j++) {
                    if (j < apps.size()) {
                        routineApps.add(RoutineApp.builder()
                                .routine(routine)
                                .app(apps.get(j))
                                .build());
                    }
                }
                routine.setRoutineApps(routineApps);
            }
            // 루틴 태그 자동 연결 (1~3개)
            Collections.shuffle(tags);
            int tagCount = random.nextInt(3) + 1;
            List<RoutineTag> routineTags = new ArrayList<>();
            for (int j = 0; j < tagCount; j++) {
                routineTags.add(RoutineTag.builder()
                        .routine(routine)
                        .tag(tags.get(j))
                        .build());
            }
            routine.setRoutineTags(routineTags);

            // 루틴 스케줄 자동 생성
            List<RoutineSchedule> schedules = new ArrayList<>();
            Set<DayOfWeek> scheduledDays = new HashSet<>();
            int dayCount = random.nextInt(7) + 1;
            for (int j = 0; j < dayCount; j++) {
                scheduledDays.add(DayOfWeek.values()[random.nextInt(7)]);
            }
            for (DayOfWeek day : scheduledDays) {
                schedules.add(RoutineSchedule.builder().routine(routine).dayOfWeek(day).build());
            }
            routine.setRoutineSchedules(schedules);

            routineBatch.add(routine);

            // BATCH_SIZE(1000개)가 모일 때마다 DB에 저장하고, 메모리를 비웁니다.
            if (routineBatch.size() >= BATCH_SIZE) {
                List<Routine> savedRoutines = routineRepository.saveAll(routineBatch);
                allGeneratedRoutines.addAll(savedRoutines);
                routineBatch.clear();
                log.info("{}개의 루틴 중간 저장 완료...", allGeneratedRoutines.size());
            }
        }
        // 루프가 끝난 후 남은 루틴들을 저장합니다.
        if (!routineBatch.isEmpty()) {
            List<Routine> savedRoutines = routineRepository.saveAll(routineBatch);
            allGeneratedRoutines.addAll(savedRoutines);
        }

        return allGeneratedRoutines;
    }

    private void createBulkRelationsAndLogs(int count, List<User> users, List<Tag> tags, List<Routine> routines) {
        // 팔로우 관계 생성
        Set<String> existingFollows = new HashSet<>();
        List<UserFollow> followsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User follower = users.get(random.nextInt(users.size()));
            User following = users.get(random.nextInt(users.size()));

            if (follower.getId().equals(following.getId())) continue;

            String followKey = follower.getId() + ":" + following.getId();
            if (existingFollows.contains(followKey)) continue;

            followsToSave.add(UserFollow.builder().follower(follower).following(following).build());
            existingFollows.add(followKey);
        }
        userFollowRepository.saveAll(followsToSave);

        // 선호 태그 관계 생성
        Set<String> existingFavoriteTags = new HashSet<>();
        List<UserFavoriteTag> favoriteTagsToSave = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Tag tag = tags.get(random.nextInt(tags.size()));

            String favoriteKey = user.getId() + ":" + tag.getId();
            if (existingFavoriteTags.contains(favoriteKey)) continue;

            favoriteTagsToSave.add(UserFavoriteTag.builder().user(user).tag(tag).build());
            existingFavoriteTags.add(favoriteKey);
        }
        userFavoriteTagRepository.saveAll(favoriteTagsToSave);

        // 1. 로그를 생성할 루틴들로부터 스냅샷을 먼저 만듭니다.
        List<RoutineSnapshot> snapshotsToSave = new ArrayList<>();
        for (Routine routine : routines) {
            if (random.nextInt(10) == 0) { // 10% 확률로 스냅샷 생성
                RoutineSnapshot snapshot = RoutineSnapshot.builder()
                        .originalRoutineId(routine.getId())
                        .title(routine.getTitle())
                        .content(routine.getContent())
                        .imageUrl(routine.getImageUrl())
                        .isSimple(routine.isSimple())
                        .isUserVisible(routine.isUserVisible())
                        .requiredTime(routine.getRequiredTime())
                        .build();

                // 스텝 스냅샷 생성 및 연결
                List<RoutineStepSnapshot> stepSnapshots = routine.getRoutineSteps().stream()
                        .map(step -> RoutineStepSnapshot.builder()
                                .routineSnapshot(snapshot) // 부모-자식 관계 설정
                                .name(step.getName())
                                .stepOrder(step.getStepOrder())
                                .estimatedTime(step.getEstimatedTime())
                                .build())
                        .collect(Collectors.toList());
                snapshot.getStepSnapshots().addAll(stepSnapshots);

                // 태그 스냅샷 생성 및 연결
                List<RoutineTagSnapshot> tagSnapshots = routine.getRoutineTags().stream()
                        .map(routineTag -> RoutineTagSnapshot.builder()
                                .routineSnapshot(snapshot) // 부모-자식 관계 설정
                                .tagName(routineTag.getTag().getName())
                                .build())
                        .collect(Collectors.toList());
                snapshot.getTagSnapshots().addAll(tagSnapshots);

                snapshotsToSave.add(snapshot);
            }
        }
        List<RoutineSnapshot> savedSnapshots = routineSnapshotRepository.saveAll(snapshotsToSave);

        // 2. 저장된 스냅샷을 기반으로 실제 로그를 생성합니다.
        List<RoutineLog> logsToSave = new ArrayList<>();
        Map<UUID, User> routineUserMap = routines.stream()
                .collect(Collectors.toMap(Routine::getId, Routine::getUser, (u1, u2) -> u1));

        for (RoutineSnapshot snapshot : savedSnapshots) {
            User user = routineUserMap.get(snapshot.getOriginalRoutineId());
            if (user == null) continue;

            LocalDateTime startedAt = LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24));
            boolean isCompleted = random.nextBoolean();
            LocalDateTime endedAt = null;
            Duration totalTime = null;

            if (isCompleted && snapshot.getRequiredTime() != null) {
                endedAt = startedAt.plus(snapshot.getRequiredTime()).plusMinutes(random.nextInt(10) - 5); // 약간의 오차
                totalTime = Duration.between(startedAt, endedAt);
            }

            logsToSave.add(RoutineLog.builder()
                    .user(user)
                    .routineSnapshot(snapshot)
                    .startedAt(startedAt)
                    .endedAt(endedAt)
                    .totalTime(totalTime)
                    .isSimple(snapshot.isSimple())
                    .isCompleted(isCompleted)
                    .build());
        }
        routineLogRepository.saveAll(logsToSave);
    }
}
