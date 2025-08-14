package com.moru.backend.global.dummydata.seeder;

import com.moru.backend.domain.meta.domain.Tag;
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

import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocialRelationSeeder {
    private final UserFollowRepository userFollowRepository;
    private final RoutineUserActionRepository routineUserActionRepository;
    private final UserFavoriteTagRepository userFavoriteTagRepository;

    private final Random random = new Random();
    private static final int ACTION_BATCH_SIZE = 200;
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

        for (int i = 0; i < count; i++) {
            User follower = users.get(random.nextInt(users.size()));
            User following = users.get(random.nextInt(users.size()));
            if (follower.getId().equals(following.getId())) continue;

            String followKey = follower.getId() + ":" + following.getId();
            if (existingFollows.contains(followKey)) continue;

            followsToSave.add(UserFollow.builder().follower(follower).following(following).build());
            existingFollows.add(followKey);
        }
        List<UserFollow> savedFollows = userFollowRepository.saveAll(followsToSave);
        log.info("{}개의 팔로우 관계 저장 완료", savedFollows.size());
        return savedFollows;
    }

    /**
     * 스크랩 액션 생성 및 저장
     * @param count     생성할 액션 수
     * @param users     사용자 리스트
     * @param routines  루틴 리스트
     */
    @Transactional
    public List<RoutineUserAction> createScrapActions(int count, List<User> users, List<Routine> routines) {
        if (count <= 0 || users.isEmpty() || routines.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> existingScraps = new HashSet<>();
        List<RoutineUserAction> actionsToSave = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User user = users.get(random.nextInt(users.size()));
            Routine routine = routines.get(random.nextInt(routines.size()));

            // 1. 자기 자신의 루틴은 스크랩 불가
            if (user.getId().equals(routine.getUser().getId())) {
                continue;
            }

            // 2. 중복 스크랩 방지
            String scrapKey = user.getId() + ":" + routine.getId();
            if (existingScraps.contains(scrapKey)) {
                continue;
            }

            actionsToSave.add(RoutineUserAction.builder().user(user).routine(routine).actionType(ActionType.SCRAP).build());
            existingScraps.add(scrapKey);
        }
        List<RoutineUserAction> savedActions = routineUserActionRepository.saveAll(actionsToSave);
        log.info("{}개의 스크랩 액션 저장 완료", savedActions.size());
        return savedActions;
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

        List<RoutineUserAction> actionBatch = new ArrayList<>(ACTION_BATCH_SIZE);

        for(Routine routine : routines) {
            // 0 - Min(MAX_LIKES_PER_ROUTINE, 유저수-1) 만큼 좋아요 생성 (자신 제외)
            int maxLikes = Math.min(MAX_LIKES_PER_ROUTINE, allUsers.size() - 1);
            int likeCount = random.nextInt(maxLikes + 1);
            if(likeCount == 0) {
                continue;
            }

            // 루틴 소유자를 제외한 유저 리스트를 섞고 앞에서 likeCount명 사용
            List<User> likableUsers = new ArrayList<>(allUsers);
            likableUsers.removeIf(u -> u.getId().equals(routine.getUser().getId()));
            Collections.shuffle(likableUsers, random);

            List<User> pickedUsers = likableUsers.subList(0, Math.min(likeCount, likableUsers.size()));

            for (User u : pickedUsers) {
                actionBatch.add(RoutineUserAction.builder()
                        .routine(routine)
                        .user(u)
                        .actionType(ActionType.LIKE)
                        .build());
            }
            // 루틴의 likeCount를 실제 생성량으로 동기화
            routine.setLikeCount(pickedUsers.size());
        }

        routineUserActionRepository.saveAll(actionBatch);
        log.info("{}개의 루틴에 대한 좋아요 액션 저장 완료", routines.size());
    }
}
