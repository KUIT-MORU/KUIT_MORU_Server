package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineScheduleHistoryRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.routine.domain.schedule.RoutineScheduleHistory;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.dummydata.DummyDataPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineSeeder {

    private final RoutineRepository routineRepository;
    private final RoutineScheduleHistoryRepository routineScheduleHistoryRepository;
    private final DummyDataPool dummyDataPool;

    private final Faker faker = new Faker(new Locale("ko"));
    private final Random random = new Random();
    private static final int BATCH_SIZE = 200;

    @Transactional
    public List<Routine> createBulkRoutines(int count, List<User> users, List<Tag> tags, List<App> apps) {
        List<Routine> allGeneratedRoutines = new ArrayList<>();
        List<Routine> routineBatch = new ArrayList<>();
        List<RoutineScheduleHistory> allGeneratedRoutineScheduleHistories = new ArrayList<>();
        List<RoutineScheduleHistory> routineScheduleHistoryBatch = new ArrayList<>();

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
                    .likeCount(0)
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
            RoutineScheduleHistory history = createAndAddSchedules(routine);

            routineBatch.add(routine);
            routineScheduleHistoryBatch.add(history);

            if (routineBatch.size() >= BATCH_SIZE) {
                allGeneratedRoutines.addAll(routineRepository.saveAll(routineBatch));
                routineBatch.clear();
                log.info("{}개의 루틴 중간 저장 완료...", allGeneratedRoutines.size());
            }
            if (routineScheduleHistoryBatch.size() >= BATCH_SIZE) {
                allGeneratedRoutineScheduleHistories.addAll(
                        routineScheduleHistoryRepository.saveAll(
                                routineScheduleHistoryBatch
                        )
                );
                routineScheduleHistoryBatch.clear();
                log.info("{}개의 루틴 스케줄 히스토리 중간 저장 완료...", allGeneratedRoutineScheduleHistories.size());
            }
        }
        if (!routineBatch.isEmpty()) {
            allGeneratedRoutines.addAll(routineRepository.saveAll(routineBatch));
        }
        if (!routineScheduleHistoryBatch.isEmpty()) {
            allGeneratedRoutineScheduleHistories.addAll(
                    routineScheduleHistoryRepository.saveAll(
                            routineScheduleHistoryBatch
                    )
            );
        }
        return allGeneratedRoutines;
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

        int stepCount = random.nextInt(4) + 3;
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
    private RoutineScheduleHistory createAndAddSchedules(Routine routine) {
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

        // 루틴 히스토리 설정
        LocalDateTime effectiveStartDateTime = LocalDateTime.now().minusDays(30);
        return RoutineScheduleHistory.builder()
                .routine(routine)
                .scheduledDays(scheduledDays.stream().toList())
                .effectiveStartDateTime(effectiveStartDateTime)
                .build();
        // --- 로직 종료 ---
    }
}
