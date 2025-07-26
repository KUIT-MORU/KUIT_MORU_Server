package com.moru.backend.global.config.dummy;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineAppSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.meta.dao.AppRepository;
import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class DummyDataGenerator {
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final AppRepository appRepository;
    private final RoutineRepository routineRepository;
    private final UserFollowRepository userFollowRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;
    private final DummyDataPool dummyDataPool;

    // Faker 인스턴스와 Random 객체를 필드로 선언해서 재사용하기
    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();

    // 테스트용 공통 비밀번호 (실제 암호화된 값)
    private static final String COMMON_PASSWORD_HASH = "$2a$12$/OXNM8oYy5chh/iOUA3j3.XjIEYi9Zbg/kiVT3.D/.zP2cev/5EDq"; // 1234abcde!@
    private static final int BATCH_SIZE = 200; // 배치 크기를 상수로 관리

    @Transactional(readOnly = true)
    public boolean isDataPresent() {
        return userRepository.count() > 0;
    }

    @Transactional
    public List<Tag> createManualTags() {
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

    @Transactional
    public List<App> createManualApps() {
        List<App> apps = Arrays.asList(
                App.builder().name("카카오톡").packageName("com.kakao.talk").build(),
                App.builder().name("인스타그램").packageName("com.instagram.android").build(),
                App.builder().name("유튜브").packageName("com.google.android.youtube").build(),
                App.builder().name("네이버").packageName("com.naver.app").build()
        );
        return appRepository.saveAll(apps);
    }

    @Transactional
    public List<User> createBulkUsers(int count) {
        List<User> allGeneratedUsers = new ArrayList<>();
        List<User> userBatch = new ArrayList<>();

        // 1. 테스트용 고정 사용자 추가 (data.sql 내용 반영)
        User testUser = User.builder()
                .email("test@example.com")
                .password("$2a$10$xUBrBGPuFIPdnDU2LCRLOeb3ML.vcGEen7NrughMZcQAs/i4cbxsy")
                .nickname("테스트유저")
                .gender(Gender.MALE)
                .birthday(LocalDate.parse("2000-01-01"))
                .bio("테스트 계정입니다.")
                .profileImageUrl("https://example.com/profile0.jpg")
                // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                .build();
        userBatch.add(testUser);

        for (int i = 1; i < count; i++) {
            // 예측 가능한 고유 이메일과 닉네임 생성
            String email = "user" + i + "@moru.com";
            String nickname = "모루유저" + i;

            LocalDate birthday = faker.date().birthday(18, 65).toInstant()
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            userBatch.add(User.builder()
                    .email(email)
                    .password(COMMON_PASSWORD_HASH)
                    .nickname(nickname)
                    .gender(random.nextBoolean() ? Gender.MALE : Gender.FEMALE)
                    .birthday(birthday)
                    .bio(dummyDataPool.getRandomBio())
                    // profileImageUrl은 faker로 직접 생성하여 설정
                    .profileImageUrl(random.nextInt(10) < 7 ? faker.avatar().image() : null)
                    // status, createdAt, updatedAt은 자동 처리되므로 설정 불필요
                    .build());
            if (userBatch.size() >= BATCH_SIZE) {
                allGeneratedUsers.addAll(userRepository.saveAll(userBatch));
                userBatch.clear();
                log.info("{}명의 사용자 중간 저장 완료...", allGeneratedUsers.size());
            }
        }
        if (!userBatch.isEmpty()) {
            allGeneratedUsers.addAll(userRepository.saveAll(userBatch));
        }
        return allGeneratedUsers;
    }

    @Transactional
    public List<Routine> createBulkRoutines(int count, List<User> users, List<Tag> tags, List<App> apps) {
        List<Routine> allGeneratedRoutines = new ArrayList<>();
        List<Routine> routineBatch = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            // em.getReference()를 사용하여 실제 SELECT 없이 프록시 객체를 가져옴 (N+1 방지)
            User owner = users.get(random.nextInt(users.size()));
            boolean isSimpleRoutine = random.nextBoolean(); // 단순/집중 루틴 랜덤 결정

            DummyDataPool.RoutineRecipe recipe = dummyDataPool.getRandomRecipe();
            String title = recipe.getTitles().get(random.nextInt(recipe.getTitles().size()));
            String content = String.format(recipe.getContentTemplates().get(random.nextInt(recipe.getContentTemplates().size())), recipe.getTheme());


            Routine routine = Routine.builder()
                    .user(owner)
                    .title(title)
                    .content(content)
                    .isSimple(isSimpleRoutine)
                    .isUserVisible(random.nextBoolean())
                    .likeCount(random.nextInt(500))
                    .viewCount(random.nextInt(2000))
                    // requiredTime은 아래에서 스텝 시간 합계로 계산되므로 여기서는 설정하지 않음
                    .imageUrl(random.nextInt(10) < 3 ? faker.avatar().image() : null) // 30% 확률로 이미지 URL 추가
                    .status(true)
                    .build();
            Duration totalRequiredTime = createAndAddSteps(routine, isSimpleRoutine, recipe);
            // 계산된 총 소요 시간을 루틴에 설정 (집중 루틴만)
            if (!isSimpleRoutine) {
                routine.setRequiredTime(totalRequiredTime);
                // 집중 루틴일때만 앱 연결
                connectAppsToRoutine(routine, apps);
            }
            connectTagsToRoutine(routine, tags, recipe.getTheme());
            createAndAddSchedules(routine);

            routineBatch.add(routine);

            if (routineBatch.size() >= BATCH_SIZE) {
                allGeneratedRoutines.addAll(routineRepository.saveAll(routineBatch));
                routineBatch.clear();
                log.info("{}개의 루틴 중간 저장 완료...", allGeneratedRoutines.size());
            }
        }
        if (!routineBatch.isEmpty()) {
            allGeneratedRoutines.addAll(routineRepository.saveAll(routineBatch));
        }
        return allGeneratedRoutines;
    }

    /**
     * 팔로우 관계 생성 및 저장
     * @param count 생성할 관계 수
     * @param users 사용자 리스트
     */
    @Transactional
    public void createFollowRelations(int count, List<User> users) {
        if (count <= 0 ) return;
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
        log.info("{}개의 팔로우 관계 저장 완료", followsToSave.size());
    }

    /**
     * 선호 태그 관계 생성 및 저장
     * @param count 생성할 관계 수
     * @param users 사용자 리스트
     * @param tags  태그 리스트
     */
    @Transactional
    public void createFavoriteTagRelations(int count, List<User> users, List<Tag> tags) {
        if (count <= 0 ) return;

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
        log.info("{}개의 선호 태그 저장 완료", favoriteTagsToSave.size());
    }

    /**
     * 루틴 로그와 스냅샷을 생성하고 저장
     * @param routines 루틴 리스트
     */
    @Transactional
    public void createLogs(List<Routine> routines) {
        List<RoutineSnapshot> savedSnapshots = createSnapshots(routines);
        createLogsFromSnapshots(savedSnapshots, routines);
    }

    //====헬퍼 메서드====//

    /**
     * routine에 스텝 생성 & 추가 -> 총 소요시간을 계산하여 반환.
     * @param routine           대상 루틴
     * @param isSimpleRoutine   단순 루틴 여부
     * @return                  총 소요 시간 (단순 루틴이면 Duration.ZERO)
     */
    private Duration createAndAddSteps(Routine routine, boolean isSimpleRoutine, DummyDataPool.RoutineRecipe recipe) {
        // --- 스텝 생성 및 시간 계산 로직 ---
        List<String> allCategories = Stream.concat(
                recipe.getCoreStepCategories().stream(),
                recipe.getSecondaryStepCategories().stream()
        ).collect(Collectors.toList());

        int stepCount = random.nextInt(3) + 3;
        Set<String> stepNames = dummyDataPool.getRandomStepsFromCategories(allCategories, stepCount);

        Duration totalRequiredTime = Duration.ZERO; // 총 소요시간 초기화
        int stepOrder = 1;

        for (String stepName : stepNames) {
            RoutineStep.RoutineStepBuilder stepBuilder = RoutineStep.builder()
                    .name(stepName)
                    .stepOrder(stepOrder++);

            if (!isSimpleRoutine) {
                Duration estimatedTime = Duration.ofMinutes(random.nextInt(30) + 1);
                stepBuilder.estimatedTime(estimatedTime);
                totalRequiredTime = totalRequiredTime.plus(estimatedTime);
            }
            routine.addRoutineStep(stepBuilder.build());
        }
        return totalRequiredTime;
    }

    /**
     * 루틴에 1-4개의 앱 연결 (앱이 없으면 사용 가능한 앱이 없으면 그냥 반환)
     * @param routine   대상 루틴
     * @param apps      연결할 앱 리스트
     */
    private void connectAppsToRoutine(Routine routine, List<App> apps) {
        if (apps.isEmpty()) return;

        Collections.shuffle(apps);
        int appCount = random.nextInt(4) + 1; // 1~4개 앱 연결
        for (int j = 0; j < Math.min(appCount, apps.size()); j++) {
            if (j < apps.size()) {
                RoutineApp routineApp = RoutineApp.builder()
                        .app(apps.get(j))
                        .build();
                routine.addRoutineApp(routineApp);
            }
        }
    }

    /**
     * 루틴에 1-3개의 태그 연결 (사용 가능한 태그가 없으면 그냥 반환)
     * @param routine   대상 루틴
     * @param tags      연결할 태그 리스트
     */
    private void connectTagsToRoutine(Routine routine, List<Tag> tags, String theme) {
        if (tags.isEmpty()) return;

        Set<Tag> tagsToConnect = new HashSet<>();

        // 테마와 일치하는 태그를 우선적으로 추가
        tags.stream()
                .filter(tag -> tag.getName().equals(theme))
                .findFirst()
                .ifPresent(tagsToConnect::add);

        // 나머지 태그를 랜덤하게 추가하여 총 1~3개를 맞춤
        Collections.shuffle(tags);
        int tagCount = random.nextInt(3) + 1;
        for (Tag tag : tags) {
            if (tagsToConnect.size() >= tagCount) {
                break;
            }
            tagsToConnect.add(tag);
        }

        for (Tag tag : tagsToConnect) {
            RoutineTag routineTag = RoutineTag.builder()
                    .tag(tag)
                    .build();
            routine.addRoutineTag(routineTag);
        }
    }

    /**
     * 루틴에 무작위 스케쥴 생성 및 추가
     * @param routine   대상 루틴
     */
    private void createAndAddSchedules(Routine routine) {
        // 루틴 스케줄 자동 생성
        Set<DayOfWeek> scheduledDays = new HashSet<>();
        int dayCount = random.nextInt(7) + 1;
        for (int j = 0; j < dayCount; j++) {
            scheduledDays.add(DayOfWeek.values()[random.nextInt(7)]);
        }
        for (DayOfWeek day : scheduledDays) {
            // Generate a random time for the schedule
            java.sql.Time randomTime = new java.sql.Time(faker.date().future(1, java.util.concurrent.TimeUnit.HOURS).getTime());

            RoutineSchedule schedule = RoutineSchedule.builder()
                    .dayOfWeek(day)
                    .time(randomTime.toLocalTime()) // Set the non-null time value
                    .alarmEnabled(random.nextBoolean()) // Also set a value for alarm_enabled
                    .build();
            // 편의 메서드를 사용하여 관계를 설정합니다.
            routine.addRoutineSchedule(schedule);
        }
        // --- 로직 종료 ---
    }

    /**
     * 루틴 스냅샷을 생성하고 저장
     * @param routines  루틴 리스트
     * @return          생성 및 저장된 루틴 스냅샷 리스트
     */
    private List<RoutineSnapshot> createSnapshots(List<Routine> routines) {
        // 1. 로그를 생성할 루틴들로부터 스냅샷을 먼저 만듭니다.
        List<RoutineSnapshot> snapshotsToSave = routines.stream()
                .filter(routine -> random.nextInt(10) == 0) // 10% 확률로 스냅샷 생성 결정
                .map(this::buildSnapshotFromRoutine)      // 실제 생성 로직은 헬퍼 메서드에 위임
                .collect(Collectors.toList());
        List<RoutineSnapshot> savedSnapshots = routineSnapshotRepository.saveAll(snapshotsToSave);
        log.info("루틴 스냅샷 {}개 저장 완료", savedSnapshots.size());
        return savedSnapshots;
    }

    /**
     * Routine 객체에 스텝, 태그, 앱을 포함하는
     * 완전한 RoutineSnapshot 객체 생성
     * @param routine   원본 루틴
     * @return          생성된 RoutineSnapshot 객체
     */
    private RoutineSnapshot buildSnapshotFromRoutine(Routine routine) {
        // 부모 스냅샷 객체를 먼저 생성
        RoutineSnapshot snapshot = RoutineSnapshot.builder()
                .originalRoutineId(routine.getId())
                .title(routine.getTitle())
                .content(routine.getContent())
                .imageUrl(routine.getImageUrl())
                .isSimple(routine.isSimple())
                .isUserVisible(routine.isUserVisible())
                .requiredTime(routine.getRequiredTime())
                .build();

        // 자식 스냅샷(스텝)을 생성하고 부모에 연결
        List<RoutineStepSnapshot> stepSnapshots = routine.getRoutineSteps().stream()
                .map(step -> RoutineStepSnapshot.builder()
                        .routineSnapshot(snapshot) // 양방향 관계 설정
                        .name(step.getName())
                        .stepOrder(step.getStepOrder())
                        .estimatedTime(step.getEstimatedTime())
                        .build())
                .collect(Collectors.toList());
        snapshot.getStepSnapshots().addAll(stepSnapshots);

        // 자식 스냅샷(태그)을 생성하고 부모에 연결
        List<RoutineTagSnapshot> tagSnapshots = routine.getRoutineTags().stream()
                .map(rt -> RoutineTagSnapshot.builder()
                        .routineSnapshot(snapshot) // 양방향 관계 설정
                        .tagName(rt.getTag().getName())
                        .build())
                .collect(Collectors.toList());
        snapshot.getTagSnapshots().addAll(tagSnapshots);

        // 자식 스냅샷(앱)을 생성하고 부모에 연결
        List<RoutineAppSnapshot> appSnapshots = routine.getRoutineApps().stream()
                .map(ra -> RoutineAppSnapshot.builder()
                        .routineSnapshot(snapshot) // 양방향 관계 설정
                        .name(ra.getApp().getName())
                        .packageName(ra.getApp().getPackageName())
                        .build())
                .collect(Collectors.toList());
        snapshot.getAppSnapshots().addAll(appSnapshots);

        return snapshot;
    }

    /**
     * 저장된 스냅샷을 기반으로 로그를 생성 및 저장
     * @param savedSnapshots    저장된 스냅샷 리스트
     * @param routines          원본 루틴 리스트
     */
    private void createLogsFromSnapshots(List<RoutineSnapshot> savedSnapshots, List<Routine> routines) {
        if (savedSnapshots.isEmpty()) {
            log.info("생성된 스냅샷이 없음 -> 루틴 로그 생성 x");
            return;
        }
        // 데이터 준비
        Map<UUID, User> routineUserMap = routines.stream()
                .collect(Collectors.toMap(Routine::getId, Routine::getUser, (u1, u2) -> u1));

        // 개별 로그 생성
        List<RoutineLog> logsToSave = new ArrayList<>();
        for (RoutineSnapshot snapshot : savedSnapshots) {
            User user = routineUserMap.get(snapshot.getOriginalRoutineId());
            if (user != null) {
                logsToSave.add(buildRoutineLog(snapshot, user));
            }
        }
        batchSaveLogs(logsToSave);
    }

    /**
     * 스냅샷과 사용자 기반으로 단일 ROutineLog 객체 생성
     * @param snapshot  로그의 기반이 될 스냅샷
     * @param user      로그의 소유자
     * @return          생성된 RoutineLog 객체
     */
    private RoutineLog buildRoutineLog(RoutineSnapshot snapshot, User user) {
        LocalDateTime startedAt = LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24));
        boolean isCompleted = random.nextBoolean();
        LocalDateTime endedAt = null;
        Duration totalTime = null;

        if (isCompleted && snapshot.getRequiredTime() != null) {
            endedAt = startedAt.plus(snapshot.getRequiredTime()).plusMinutes(random.nextInt(10) - 5); // 약간의 오차
            totalTime = Duration.between(startedAt, endedAt);
        }

        return RoutineLog.builder()
                .user(user)
                .routineSnapshot(snapshot)
                .startedAt(startedAt)
                .endedAt(endedAt)
                .totalTime(totalTime)
                .isSimple(snapshot.isSimple())
                .isCompleted(isCompleted)
                .build();
    }

    /**
     * 주어진 RoutineLog 리스트를 배치 단위로 나누어 저장
     * @param logsToSave    저장할 로그 리스트
     */
    private void batchSaveLogs(List<RoutineLog> logsToSave) {
        if (logsToSave.isEmpty()) {
            return;
        }
        log.info("생성된 {}개의 루틴 로그를 배치 저장합니다...", logsToSave.size());
        for (int i = 0; i < logsToSave.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, logsToSave.size());
            List<RoutineLog> batch = logsToSave.subList(i, end);
            routineLogRepository.saveAll(batch);
            log.info("▶ {}/{} 개의 루틴 로그 저장 완료", end, logsToSave.size());
        }
        log.info("루틴 로그 저장 완료");
    }
}
