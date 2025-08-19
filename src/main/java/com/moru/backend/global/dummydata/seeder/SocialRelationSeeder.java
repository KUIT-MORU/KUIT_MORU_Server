package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.routine.RoutineRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.dao.UserFollowRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.social.domain.UserFollow;
import com.moru.backend.domain.user.dao.UserFavoriteTagRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.domain.user.domain.UserFavoriteTag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialRelationSeeder {
    private final UserFollowRepository userFollowRepository;
    private final RoutineUserActionRepository routineUserActionRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;
    private final RoutineRepository routineRepository;

    private final Random random = new Random();
    private static final int BATCH_SIZE = 200;
    private static final int MAX_LIKES_PER_ROUTINE = 50;

    /**
     * 팔로우 관계 생성 및 저장
     * @param count 생성할 관계 수
     * @param users 사용자 리스트
     */
    @Transactional
    public List<UserFollow> createFollowRelations(int count, List<User> users) {
        if (count <= 0 ) return Collections.emptyList();
        // 팔로우 관계 생성
        Set<String> existingFollows = new HashSet<>();
        List<UserFollow> followsToSave = new ArrayList<>();
        List<UserFollow> allSavedFollows = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User follower = users.get(random.nextInt(users.size()));
            User following = users.get(random.nextInt(users.size()));
            if (follower.getId().equals(following.getId())) continue;

            String followKey = follower.getId() + ":" + following.getId();
            if (existingFollows.contains(followKey)) continue;

            followsToSave.add(UserFollow.builder().follower(follower).following(following).build());
            existingFollows.add(followKey);

            if (followsToSave.size() >= BATCH_SIZE) {
                allSavedFollows.addAll(userFollowRepository.saveAll(followsToSave));
                followsToSave.clear();
            }
        }
        if (!followsToSave.isEmpty()) {
            allSavedFollows.addAll(userFollowRepository.saveAll(followsToSave));
        }
        log.info("{}개의 팔로우 관계 저장 완료", allSavedFollows.size());
        return allSavedFollows;
    }

    /**
     * 스크랩 액션 생성 및 저장
     * @param count     생성할 액션 수
     * @param users     사용자 리스트
     * @param routines  루틴 리스트
     */
    @Transactional
    public List<RoutineUserAction> createScrapActionsGuaranteed(int count,
                                                                List<User> users,
                                                                List<Routine> routines) {
        if (count <= 0 || users == null || users.isEmpty() || routines == null || routines.isEmpty()) {
            log.info("SCRAP 생성 스킵: count<=0 또는 입력 비어있음");
            return Collections.emptyList();
        }

        // 0) 캐시/준비
        Map<UUID, UUID> routineOwnerId = new HashMap<>(routines.size());
        for (Routine r : routines) {
            routineOwnerId.put(r.getId(), r.getUser().getId());
        }
        List<UUID> userIds = users.stream().map(User::getId).toList();
        List<UUID> routineIds = routines.stream().map(Routine::getId).toList();

        // 1) 유효 후보 (user != routine.owner)
        List<AbstractMap.SimpleEntry<UUID, UUID>> candidates = new ArrayList<>();
        for (UUID uid : userIds) {
            for (UUID rid : routineIds) {
                UUID ownerId = routineOwnerId.get(rid);
                if (!uid.equals(ownerId)) {
                    candidates.add(new AbstractMap.SimpleEntry<>(uid, rid));
                }
            }
        }
        if (candidates.isEmpty()) {
            log.warn("SCRAP 후보가 0개임(모든 루틴이 자기 소유자만 존재).");
            return Collections.emptyList();
        }

        // 2) DB에 이미 존재하는 (user,routine) SCRAP 제거 (LIKE는 허용)
        List<Object[]> existing = routineUserActionRepository.findExistingPairs(ActionType.SCRAP, userIds, routineIds);

        Set<String> existingKeys = new HashSet<>(Math.max(16, existing.size() * 2));
        for (Object[] row : existing) {
            UUID uid = (UUID) row[0];
            UUID rid = (UUID) row[1];
            existingKeys.add(uid + ":" + rid);
        }

        List<AbstractMap.SimpleEntry<UUID, UUID>> filtered = new ArrayList<>(candidates.size());
        for (var e : candidates) {
            if (!existingKeys.contains(e.getKey() + ":" + e.getValue())) {
                filtered.add(e);
            }
        }
        if (filtered.isEmpty()) {
            log.warn("신규로 생성 가능한 SCRAP 후보가 없음(모두 기존과 중복).");
            return Collections.emptyList();
        }

        // 진단 로그: 어디에서 줄어드는지 즉시 확인
        log.info("SCRAP candidates={}, existingPairs(SCRAP)={}, filtered={}",
                candidates.size(), existing.size(), filtered.size());

        // 3) 셔플 & 타깃 개수 결정
        Collections.shuffle(filtered, random);
        int target = Math.min(count, filtered.size());
        if (target < count) {
            log.warn("요청 {}개 중 {}개만 생성 가능(고유 조합 부족).", count, target);
        }

        // 4) Top-up 루프: 부분 실패가 있어도 target까지 채움
        Map<UUID, User> userMap = new HashMap<>(users.size());
        users.forEach(u -> userMap.put(u.getId(), u));
        Map<UUID, Routine> routineMap = new HashMap<>(routines.size());
        routines.forEach(r -> routineMap.put(r.getId(), r));

        List<RoutineUserAction> savedAll = new ArrayList<>(target);
        int idx = 0;

        while (savedAll.size() < target && idx < filtered.size()) {
            List<RoutineUserAction> buffer = new ArrayList<>(Math.min(BATCH_SIZE, target - savedAll.size()));

            while (buffer.size() < BATCH_SIZE && savedAll.size() + buffer.size() < target && idx < filtered.size()) {
                var pair = filtered.get(idx++);
                User u = userMap.get(pair.getKey());
                Routine r = routineMap.get(pair.getValue());
                if (u == null || r == null) continue; // 안전장치

                buffer.add(RoutineUserAction.builder()
                        .user(u)
                        .routine(r)
                        .actionType(ActionType.SCRAP)
                        .build());
            }

            try {
                List<RoutineUserAction> saved = routineUserActionRepository.saveAll(buffer);
                savedAll.addAll(saved);
            } catch (Exception e) {
                log.warn("SCRAP 배치 저장 일부 실패: {} (buffer={}, progress={}/{})",
                        e.getMessage(), buffer.size(), savedAll.size(), target);
                // 필요 시 buffer를 더 잘게 쪼개 재시도 로직 추가 가능
            }
        }

        log.info("SCRAP 생성 완료: 요청={}, 실제 저장={}", count, savedAll.size());
        return savedAll;
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
        int savedCount = 0;

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

            if (favoriteTagsToSave.size() >= BATCH_SIZE) {
                savedCount += userFavoriteTagRepository.saveAll(favoriteTagsToSave).size();
                favoriteTagsToSave.clear();
            }
        }
        if (!favoriteTagsToSave.isEmpty()) {
            savedCount += userFavoriteTagRepository.saveAll(favoriteTagsToSave).size();
        }
        log.info("{}개의 선호 태그 저장 완료", savedCount);
    }

    /**
     * 루틴들에 대해 0~{MAX_LIKES_PER_ROUTINE}개의 LIKE 액션을 랜덤 생성하고,
     * 각 루틴의 likeCount를 실제 생성 개수로 맞춘다.
     *
     * @param routines createBulkRoutines에서 방금 저장/리턴한 루틴 리스트
     * @param allUsers 모든 사용자 리스트
     */
    @Transactional
    public void seedLikesAndUpdateCount(List<Routine> routines, List<User> allUsers) {
        if(routines == null || routines.isEmpty() || allUsers == null || allUsers.isEmpty()) {
            return;
        }

        List<RoutineUserAction> actionBatch = new ArrayList<>(BATCH_SIZE);

        for(Routine routine : routines) {
            // 0 - Min(MAX_LIKES_PER_ROUTINE, 유저수-1) 만큼 좋아요 생성 (자신 제외)
            int maxLikes = Math.min(MAX_LIKES_PER_ROUTINE, allUsers.size() - 1);
            int likeCount = random.nextInt(maxLikes + 1);
            if(likeCount == 0) {
                continue;
            }

            // [개선] 1. 이 루틴에 이미 '좋아요'를 누른 사용자 ID를 DB에서 조회
            Set<UUID> existingLikers = routineUserActionRepository.findByRoutineIdAndActionType(routine.getId(), ActionType.LIKE)
                    .stream()
                    .map(action -> action.getUser().getId())
                    .collect(Collectors.toSet());

            // 루틴 소유자를 제외한 유저 리스트를 섞고 앞에서 likeCount명 사용
            // [개선] 2. 루틴 소유자 뿐만 아니라, 이미 좋아요를 누른 사용자도 후보에서 제외
            List<User> likableUsers = allUsers.stream()
                    .filter(u -> !u.getId().equals(routine.getUser().getId()) && !existingLikers.contains(u.getId()))
                    .collect(Collectors.toCollection(ArrayList::new));
            Collections.shuffle(likableUsers, random);

            List<User> pickedUsers = likableUsers.subList(0, Math.min(likeCount, likableUsers.size()));

            for (User u : pickedUsers) {
                actionBatch.add(RoutineUserAction.builder()
                        .routine(routine)
                        .user(u)
                        .actionType(ActionType.LIKE)
                        .build());

                if (actionBatch.size() >= BATCH_SIZE) {
                    routineUserActionRepository.saveAll(actionBatch);
                    actionBatch.clear();
                }
            }
            // 루틴의 likeCount를 실제 생성량으로 동기화
            routine.setLikeCount(pickedUsers.size());
        }

        // 위에서 setLikeCount 한 값들을 DB에 반영
        routineRepository.saveAll(routines);
        routineRepository.flush(); // 선택이지만 확실히 반영하고 싶으면

        if (!actionBatch.isEmpty()) {
            routineUserActionRepository.saveAll(actionBatch);
        }
        log.info("{}개의 루틴에 대한 좋아요 액션 저장 완료", routines.size());
    }
}
