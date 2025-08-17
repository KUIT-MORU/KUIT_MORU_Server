package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineAppSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RoutineLogSeeder {
    private final RoutineLogRepository routineLogRepository;
    private final RoutineSnapshotRepository routineSnapshotRepository;
    private final Random random = new Random();
    private static final int BATCH_SIZE = 200;

    private enum UserLifestyle {
        WEEKDAY_WARRIOR, //평일에 더 성실
        WEEKEND_RELAXER, // 주말에 더 성실
        BALANCED        // 균등
    }

    private final Map<UUID, Double> userDiligenceScores = new HashMap<>();
    private final Map<UUID, UserLifestyle> userLifestyles = new HashMap<>();


    /**
     * 루틴 로그와 스냅샷을 생성하고 저장
     * @param routines 루틴 리스트
     */
    @Transactional
    public void createLogs(List<Routine> routines, List<User> users) {
        if (routines.isEmpty() || users.isEmpty()) {
            log.warn("루틴 또는 사용자가 없어 로그를 생성할 수 없습니다.");
            return;
        }
        assignDiligenceScoresToUsers(users);
        assignLifestylesToUsers(users);

        Map<UUID, User> routineOwnerMap = routines.stream()
                .collect(Collectors.toMap(Routine::getId, Routine::getUser));

        // 모든 사용자 ID 수집
        List<UUID> allUserIds = users.stream()
                .map(User::getId)
                .toList();

        // 열린 로그가 있는 사용자 조회
        List<RoutineLog> activeLogs = routineLogRepository.findActiveLogsForUsers(allUserIds);
        Set<UUID> usersWithOpenLog = activeLogs.stream()
                .map(log -> log.getUser().getId())
                .collect(Collectors.toSet());

        // 열린 로그의 startedAt도 함께 맵으로 보관
        Map<UUID, LocalDateTime> openLogStartByUser = activeLogs.stream()
                .collect(Collectors.toMap(
                        rl -> rl.getUser().getId(),
                        RoutineLog::getStartedAt,
                        // 혹시 중복이면 가장 최근 걸 유지
                        (a, b) -> a.isAfter(b) ? a : b
                ));


        List<RoutineSnapshot> savedSnapshots = createSnapshots(routines);
        createLogsFromSnapshots(savedSnapshots, users, routineOwnerMap, usersWithOpenLog, openLogStartByUser);
    }


    private java.time.DayOfWeek toJavaDayOfWeek(
            com.moru.backend.domain.routine.domain.schedule.DayOfWeek d) {
        // 커스텀 enum 값 이름과 요일이 1:1로 대응한다고 가정
        // (이름이 다르면 switch로 하나씩 매핑)
        return switch (d) {
            case MON    -> java.time.DayOfWeek.MONDAY;
            case TUE   -> java.time.DayOfWeek.TUESDAY;
            case WED -> java.time.DayOfWeek.WEDNESDAY;
            case THU  -> java.time.DayOfWeek.THURSDAY;
            case FRI    -> java.time.DayOfWeek.FRIDAY;
            case SAT  -> java.time.DayOfWeek.SATURDAY;
            case SUN    -> java.time.DayOfWeek.SUNDAY;
        };
    }

    private Map<UUID, Set<java.time.DayOfWeek>> buildScheduleMap(List<Routine> routines) {
        Map<UUID, Set<java.time.DayOfWeek>> map = new HashMap<>();
        for (Routine r : routines) {
            Set<java.time.DayOfWeek> days =
                    (r.getRoutineSchedules() == null)
                            ? EnumSet.noneOf(java.time.DayOfWeek.class) // JDK8 호환
                            : r.getRoutineSchedules().stream()
                            .map(RoutineSchedule::getDayOfWeek)                // 커스텀 enum
                            .filter(Objects::nonNull)
                            .map(this::toJavaDayOfWeek)                // ← ★ 변환
                            .collect(Collectors.toCollection(
                                    () -> EnumSet.noneOf(java.time.DayOfWeek.class) // ← ★ 제네릭 명시
                            ));

            map.put(r.getId(), days);
        }
        return map;
    }

    /**
     * 루틴 스냅샷을 생성하고 저장
     * @param routines  루틴 리스트
     * @return          생성 및 저장된 루틴 스냅샷 리스트
     */
    private List<RoutineSnapshot> createSnapshots(List<Routine> routines) {
        List<RoutineSnapshot> snapshotsToSave = new ArrayList<>();

        for (Routine routine : routines) {
            // 각 루틴마다 0~5개의 로그를 무작위로 생성합니다!
            int logCount = random.nextInt(6); // 0, 1, 2, 3, 4, 5 중 하나
            if (logCount == 0) continue;

            // 로그를 생성할 횟수만큼 스냅샷을 만듭니다.
            for (int i = 0; i < logCount; i++) {
                snapshotsToSave.add(buildSnapshotFromRoutine(routine));
            }
        }

        // 배치 저장을 위해 saveAll 사용
        List<RoutineSnapshot> savedSnapshots = new ArrayList<>();
        for (int i = 0; i < snapshotsToSave.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, snapshotsToSave.size());
            savedSnapshots.addAll(routineSnapshotRepository.saveAll(snapshotsToSave.subList(i, end)));
        }

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
                .imageUrl(Optional.ofNullable(routine.getImageUrl()).orElse(""))
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
                .toList();
        snapshot.getStepSnapshots().addAll(stepSnapshots);

        // 자식 스냅샷(태그)을 생성하고 부모에 연결
        List<RoutineTagSnapshot> tagSnapshots = routine.getRoutineTags().stream()
                .map(rt -> RoutineTagSnapshot.builder()
                        .routineSnapshot(snapshot) // 양방향 관계 설정
                        .tagName(rt.getTag().getName())
                        .build())
                .toList();
        snapshot.getTagSnapshots().addAll(tagSnapshots);

        // 자식 스냅샷(앱)을 생성하고 부모에 연결
        List<RoutineAppSnapshot> appSnapshots = routine.getRoutineApps().stream()
                .map(ra -> RoutineAppSnapshot.builder()
                        .routineSnapshot(snapshot) // 양방향 관계 설정
                        .name(ra.getApp().getName())
                        .packageName(ra.getApp().getPackageName())
                        .build())
                .toList();
        snapshot.getAppSnapshots().addAll(appSnapshots);

        return snapshot;
    }

    /**
     * 저장된 스냅샷을 기반으로 로그를 생성 및 저장
     * @param savedSnapshots    저장된 스냅샷 리스트
     * @param users          사용자들
     */
    private void createLogsFromSnapshots(
            List<RoutineSnapshot> savedSnapshots,
            List<User> users,
            Map<UUID, User> routineOwnerMap,
            Set<UUID> usersWithOpenLog,
            Map<UUID, LocalDateTime> openLogStartByUser
    ) {
        if (savedSnapshots.isEmpty()) {
            log.info("생성된 스냅샷이 없음 -> 루틴 로그 생성 x");
            return;
        }

        // 개별 로그 생성
        List<RoutineLog> logsToSave = new ArrayList<>();
        for (RoutineSnapshot snapshot : savedSnapshots) {
            // 각 로그에 대해 무작위 사용자를 할당
            // [성능 개선] DB를 반복 조회하는 대신, 미리 만들어둔 Map에서 소유자 정보를 가져옵니다.
            User owner = routineOwnerMap.getOrDefault(
                    snapshot.getOriginalRoutineId(),
                    users.get(random.nextInt(users.size()))); // 혹시 못찾으면 랜덤 유저
            logsToSave.add(buildRoutineLog(snapshot, owner, usersWithOpenLog, openLogStartByUser));
        }
        batchSaveLogs(logsToSave);
    }

    /**
     * 스냅샷과 사용자 기반으로 단일 RoutineLog 객체 생성
     *
     * @param snapshot               로그의 기반이 될 스냅샷
     * @param user                   로그의 소유자
     * @param usersWithOpenLog 미완료 로그가 이미 할당된 사용자 추적용 Set
     * @return 생성된 RoutineLog 객체
     */
    private RoutineLog buildRoutineLog(RoutineSnapshot snapshot,
                                       User user,
                                       Set<UUID> usersWithOpenLog,
                                       Map<UUID, LocalDateTime> openLogStartByUser
                                       ) {
        // 1) 완료/미완료 결정 먼저
        double diligenceScore = userDiligenceScores.getOrDefault(user.getId(), 0.5);
        UserLifestyle lifestyle = userLifestyles.getOrDefault(user.getId(), UserLifestyle.BALANCED);

        double finalCompletionProbability = diligenceScore;
        double modifier = 0.25;
        var todayDow = LocalDateTime.now().getDayOfWeek();
        switch (lifestyle) {
            case WEEKDAY_WARRIOR -> finalCompletionProbability +=
                    (todayDow != java.time.DayOfWeek.SATURDAY && todayDow != java.time.DayOfWeek.SUNDAY) ? modifier : -modifier;
            case WEEKEND_RELAXER -> finalCompletionProbability +=
                    (todayDow == java.time.DayOfWeek.SATURDAY || todayDow == java.time.DayOfWeek.SUNDAY) ? modifier : -modifier;
            case BALANCED -> { /* no-op */ }
        }
        finalCompletionProbability = Math.max(0.05, Math.min(finalCompletionProbability, 0.95));

        boolean isCompleted;
        if (usersWithOpenLog.contains(user.getId())) {
            isCompleted = true; // 이미 열린 로그 있으면 무조건 완료
        } else {
            isCompleted = random.nextDouble() < finalCompletionProbability;
            if (!isCompleted) {
                usersWithOpenLog.add(user.getId()); // 이제 이 유저는 열린 로그 1개 보유
            }
        }

        // 2) isCompleted 결과에 따라 startedAt 생성 (규칙 적용)
        LocalDateTime startedAt;
        if (isCompleted) {
            // 완료 로그의 startedAt은 항상 해당 유저의 열린 로그 startedAt보다 과거여야 함
            LocalDateTime openStart = openLogStartByUser.get(user.getId());
            if (openStart != null) {
                // 열린 로그 이전의 과거 임의 시각 (최대 30일, 경계면 보호)
                long hoursBack = 1 + random.nextInt(24 * 30 - 1); // 1시간 ~ 720시간
                startedAt = openStart.minusHours(hoursBack);
            } else {
                // 이 유저에 열린 로그가 아직 없다면, 일반 과거 범위
                startedAt = LocalDateTime.now()
                        .minusDays(random.nextInt(30))
                        .minusHours(random.nextInt(24));
            }
        } else {
            // 미완료 로그 = 열린 로그 → 항상 "최근 시각"으로 만들어 최신이 되게 함
            startedAt = LocalDateTime.now().minusMinutes(1 + random.nextInt(120)); // 1~120분 전
            openLogStartByUser.put(user.getId(), startedAt); // 이후 완료 로그가 이 시각보다 과거가 되도록 참조 갱신
        }

        // 3) endedAt/totalTime
        LocalDateTime endedAt = null;
        Duration totalTime = null;
        if (isCompleted) {
            if (!snapshot.isSimple() && snapshot.getRequiredTime() != null && !snapshot.getRequiredTime().isZero()) {
                endedAt = startedAt.plus(snapshot.getRequiredTime())
                        .plusMinutes(random.nextInt(10) - 5);
                totalTime = Duration.between(startedAt, endedAt);
            } else {
                totalTime = Duration.ofMinutes(random.nextInt(5) + 1);
                endedAt = startedAt.plus(totalTime);
            }
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

    private void assignDiligenceScoresToUsers(List<User> users) {
        double mean = 0.65;   // 목표 평균 실천율: 65%
        double stdDev = 0.20; // 표준 편차: 20% (점수 분포를 더 넓게 하여 다양한 사용자 유형 생성)
        for (User user : users) {
            double diligence = (random.nextGaussian() * stdDev) + mean;
            // 생성된 값이 5% ~ 95% 범위를 벗어나지 않도록 보정합니다.
            diligence = Math.max(0.05, Math.min(diligence, 0.95));
            userDiligenceScores.put(user.getId(), diligence);
        }
        log.info("{}명의 사용자에게 정규분포를 따르는 실천율 점수(diligence) 할당 완료", users.size());
    }

    private void assignLifestylesToUsers(List<User> users) {
        for (User user : users) {
            int choice = random.nextInt(3);
            UserLifestyle lifestyle = switch (choice) {
                case 0 -> UserLifestyle.WEEKDAY_WARRIOR;
                case 1 -> UserLifestyle.WEEKEND_RELAXER;
                default -> UserLifestyle.BALANCED;
            };
            userLifestyles.put(user.getId(), lifestyle);
        }
        log.info("{}명의 사용자에게 라이프스타일 유형 할당 완료", users.size());
    }

}
