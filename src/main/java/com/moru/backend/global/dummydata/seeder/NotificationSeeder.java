package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.notification.dao.NotificationRepository;
import com.moru.backend.domain.notification.domain.Notification;
import com.moru.backend.domain.notification.domain.NotificationType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationSeeder {
    private final NotificationRepository notificationRepository;

    private final Random random = new Random();
    private static final int BATCH_SIZE = 200; // 배치 크기를 상수로 관리

    /**
     * [핵심] 팔로우 및 루틴 생성에 대한 알림 더미 데이터를 생성합니다.
     * @param allFollows    생성된 모든 팔로우 관계 리스트
     * @param allRoutines   생성된 모든 루틴 리스트
     */
    @Transactional
    public void createBulkNotifications(List<UserFollow> allFollows, List<Routine> allRoutines) {
        log.info("알림 더미 데이터 생성을 시작합니다...");
        List<Notification> notificationsToSave = new ArrayList<>();

        // 1. 팔로우 알림 생성
        // 생성된 전체 팔로우 관계 중 10%에 대해서만 알림을 생성하여 현실성을 높입니다.
        Collections.shuffle(allFollows);
        int followNotificationCount = allFollows.size() / 10;
        List<UserFollow> followsForNotification = allFollows.subList(0, Math.min(followNotificationCount, allFollows.size()));

        for (UserFollow follow : followsForNotification) {
            Notification notification = Notification.builder()
                    .receiverId(follow.getFollowing().getId())
                    .senderId(follow.getFollower().getId())
                    .type(NotificationType.FOLLOW_RECEIVED)
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30))) // 최근 30일 내 무작위 시간
                    .build();
            notificationsToSave.add(notification);
        }
        log.info("{}개의 팔로우 알림 생성 완료.", notificationsToSave.size());

        // 2. 루틴 생성 알림 생성 (Fan-out 방식)
        // Key: 유저 ID, Value: 해당 유저를 팔로우하는 사람들의 ID 리스트
        Map<UUID, List<UUID>> followersMap = new HashMap<>();
        for (UserFollow follow : allFollows) {
            followersMap.computeIfAbsent(follow.getFollowing().getId(), k -> new ArrayList<>()).add(follow.getFollower().getId());
        }

        // 전체 루틴 중 10%에 대해서만 알림을 생성
        Collections.shuffle(allRoutines);
        int routineNotificationCount = allRoutines.size() / 10;
        List<Routine> routinesForNotification = allRoutines.subList(0, Math.min(routineNotificationCount, allRoutines.size()));
        int createdRoutineNotifications = 0;

        for (Routine routine : routinesForNotification) {
            if (!routine.isUserVisible()) {
                continue; // 비공개 루틴은 알림을 보내지 않음
            }

            UUID senderId = routine.getUser().getId();
            List<UUID> followerIds = followersMap.getOrDefault(senderId, Collections.emptyList());

            for (UUID followerId : followerIds) {
                Notification notification = Notification.builder()
                        .receiverId(followerId)
                        .senderId(senderId)
                        .resourceId(routine.getId())
                        .type(NotificationType.ROUTINE_CREATED)
                        .createdAt(routine.getCreatedAt()) // 루틴 생성 시점과 동일하게
                        .build();
                notificationsToSave.add(notification);
                createdRoutineNotifications++;
            }
        }
        log.info("{}개의 루틴 생성 알림(Fan-out) 생성 완료.", createdRoutineNotifications);

        // 3. 생성된 모든 알림을 배치 저장
        if (!notificationsToSave.isEmpty()) {
            log.info("총 {}개의 알림을 배치 저장합니다...", notificationsToSave.size());
            for (int i = 0; i < notificationsToSave.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, notificationsToSave.size());
                notificationRepository.saveAll(notificationsToSave.subList(i, end));
                log.info("▶ {}/{} 개의 알림 저장 완료", end, notificationsToSave.size());
            }
            log.info("알림 더미 데이터 저장 완료.");
        }
    }

    @Transactional
    public void createUserCentricNotifications(
            User target,
            List<UserFollow> allFollows,
            List<Routine> allRoutines,
            int followNotifCount,
            int routineCreatedNotifCount
    ) {
        List<Notification> toSave = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // A. FOLLOW_RECEIVED: target을 following으로 하는 팔로우 중 일부 선택 → receiver=target
        List<UserFollow> receivedFollows = allFollows.stream()
                .filter(f -> f.getFollowing().getId().equals(target.getId()))
                .collect(Collectors.toList());
        Collections.shuffle(receivedFollows);
        for (int i = 0; i < Math.min(followNotifCount, receivedFollows.size()); i++) {
            UserFollow f = receivedFollows.get(i);
            toSave.add(Notification.builder()
                    .receiverId(target.getId())
                    .senderId(f.getFollower().getId())
                    .type(NotificationType.FOLLOW_RECEIVED)
                    .createdAt(now.minusMinutes(i))
                    .build());
        }

        // B. ROUTINE_CREATED: target이 팔로우하는 사용자의 공개 루틴 중 일부 선택 → receiver=target
        Set<UUID> followingIds = allFollows.stream()
                .filter(f -> f.getFollower().getId().equals(target.getId()))
                .map(f -> f.getFollowing().getId())
                .collect(Collectors.toSet());

        List<Routine> visibleRoutinesByFollowings = allRoutines.stream()
                .filter(r -> r.isUserVisible() && followingIds.contains(r.getUser().getId()))
                .collect(Collectors.toList());
        Collections.shuffle(visibleRoutinesByFollowings);
        for (int i = 0; i < Math.min(routineCreatedNotifCount, visibleRoutinesByFollowings.size()); i++) {
            Routine r = visibleRoutinesByFollowings.get(i);
            toSave.add(Notification.builder()
                    .receiverId(target.getId())
                    .senderId(r.getUser().getId())
                    .resourceId(r.getId())
                    .type(NotificationType.ROUTINE_CREATED)
                    .createdAt(now.minusSeconds(i))
                    .build());
        }

        if (!toSave.isEmpty()) {
            notificationRepository.saveAll(toSave);
            log.info("[UserCentric] {} 수신함 알림 {}건 생성", target.getEmail(), toSave.size());
        }
    }
}
