package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.log.dao.RoutineLogRepository;
import com.moru.backend.domain.log.dao.RoutineSnapshotRepository;
import com.moru.backend.domain.log.domain.RoutineLog;
import com.moru.backend.domain.log.domain.snapshot.RoutineAppSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import com.moru.backend.domain.log.domain.snapshot.RoutineTagSnapshot;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
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

        log.info("DB에 이미 열린 로그 보유 사용자 수: {}", usersWithOpenLog.size());


        List<RoutineSnapshot> savedSnapshots = createSnapshots(routines);
        createLogsFromSnapshots(savedSnapshots, users, routineOwnerMap, usersWithOpenLog);
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
            Set<UUID> usersWithOpenLog // Added comma here
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
            logsToSave.add(buildRoutineLog(snapshot, owner, usersWithOpenLog));
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
                                       Set<UUID> usersWithOpenLog) {
        LocalDateTime startedAt = LocalDateTime.now()
                .minusDays(random.nextInt(30))
                .minusHours(random.nextInt(24));
        LocalDateTime endedAt = null;
        Duration totalTime = null;

        // 사용자의 미리 할당된 '성실도 점수'를 가져오기
        double diligenceScore = userDiligenceScores.getOrDefault(user.getId(), 0.5); // 점수가 없으면 50% 확률

        // 사용자의 라이프스타일을 가져와 완료 확률을 조정.
        UserLifestyle lifestyle = userLifestyles.getOrDefault(user.getId(), UserLifestyle.BALANCED);
        java.time.DayOfWeek dayOfWeek = startedAt.getDayOfWeek();

        double finalCompletionProbability = diligenceScore;
        double modifier = 0.25; // 확률 보정값 (25%p)

        switch (lifestyle) {
            case WEEKDAY_WARRIOR:
                if (dayOfWeek != java.time.DayOfWeek.SATURDAY && dayOfWeek != java.time.DayOfWeek.SUNDAY) {
                    finalCompletionProbability += modifier; // 평일 보너스
                } else {
                    finalCompletionProbability -= modifier; // 주말 페널티
                }
                break;
            case WEEKEND_RELAXER:
                if (dayOfWeek == java.time.DayOfWeek.SATURDAY || dayOfWeek == java.time.DayOfWeek.SUNDAY) {
                    finalCompletionProbability += modifier; // 주말 보너스
                } else {
                    finalCompletionProbability -= modifier; // 평일 페널티
                }
                break;
            case BALANCED:
                // 확률 변경 없음
                break;
        }

        finalCompletionProbability = Math.max(0.05, Math.min(finalCompletionProbability, 0.95));

        boolean isCompleted;
        // [FIX] 이 사용자가 이미 미완료 로그를 할당받았는지 확인
        if (usersWithOpenLog.contains(user.getId())) {
            // 이미 할당받았다면, 이번 로그는 무조건 완료 처리하여 데이터 정합성을 지킴
            isCompleted = true;
        } else {
            // 아니라면, 확률에 따라 완료 여부를 결정
            isCompleted = random.nextDouble() < finalCompletionProbability;
            if (!isCompleted) {
                // 이번 로그가 미완료로 결정되면, 추적 Set에 사용자를 추가
                usersWithOpenLog.add(user.getId());
            }
        }

        // 완료된 경우에만 종료 시간과 소요 시간을 기록
        if (isCompleted) {
            if (!snapshot.isSimple() && snapshot.getRequiredTime() != null && !snapshot.getRequiredTime().isZero()) {
                // 집중 루틴: 예상 시간에 약간의 오차를 더해 현실적인 소요 시간 생성
                endedAt = startedAt.plus(snapshot.getRequiredTime())
                        .plusMinutes(random.nextInt(10) - 5);
                totalTime = Duration.between(startedAt, endedAt);
            } else {
                // 간편 루틴: 1~5분 사이의 짧은 소요 시간 생성
                totalTime = Duration.ofMinutes(random.nextInt(5) + 1);
                endedAt = startedAt.plus(totalTime);
            }
        }
        // 완료되지 않은 경우, endedAt과 totalTime은 null로 유지
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
