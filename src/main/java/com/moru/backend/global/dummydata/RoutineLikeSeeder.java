package com.moru.backend.global.dummydata;

import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor

public class RoutineLikeSeeder {
    private static final int ACTION_BATCH_SIZE = 200; // 액션 배치 크기 (상황에 맞게 조절)
    private static final int MAX_LIKES_PER_ROUTINE = 50;

    private final RoutineUserActionRepository routineUserActionRepository;

    /**
     * 새로 만든 루틴들에 대해 0~{MAX_LIKES_PER_ROUTINE}개의 LIKE 액션을 랜덤 생성하고,
     * 각 루틴의 likeCount를 실제 생성 개수로 맞춘다.
     *
     * @param routines createBulkRoutines에서 방금 저장/리턴한 루틴 리스트
     * @return 생성된 RoutineUserAction 총 개수
     */

    @Transactional
    public void seedLikesForRoutines(List<Routine> routines, List<User> allUsers) {
        if(routines == null || routines.isEmpty()) return;
        if(allUsers == null || allUsers.isEmpty()) return;

        Random rnd = new Random();

        List<RoutineUserAction> actionBatch = new ArrayList<>(ACTION_BATCH_SIZE);

        for(Routine routine : routines) {
            UUID routineId = routine.getId();
            if (routineId == null) continue;

            // 0 - Min(100, 유저수)
            int maxLikes = Math.min(MAX_LIKES_PER_ROUTINE, allUsers.size());
            int likeCount = rnd.nextInt(maxLikes+ 1);
            if(likeCount == 0) {
                continue;
            }

            // 루틴 내 중복 방지: 유저 리스트 섞고 앞에서 likeCount명 사용 (중복 없음)
            List<User> shuffled = new ArrayList<>(allUsers);
            Collections.shuffle(shuffled, rnd);
            List<User> picked = shuffled.subList(0, likeCount);

            for (User u : picked) {
                RoutineUserAction action = RoutineUserAction.builder()
                        .routine(routine)
                        .user(u)
                        .actionType(ActionType.LIKE)
                        .createdAt(LocalDateTime.now())
                        .createdAt(LocalDateTime.now())
                        .build();
                actionBatch.add(action);

                if (actionBatch.size() >= ACTION_BATCH_SIZE) {
                    routineUserActionRepository.saveAll(actionBatch);
                    actionBatch.clear();
                }
            }

            // 루틴의 likeCount를 실제 생성량으로 동기화
            routine.setLikeCount(likeCount);
        }

        if (!actionBatch.isEmpty()) {
            routineUserActionRepository.saveAll(actionBatch);
        }

        log.info("LIKE seeding done for {} routines.", routines.size());
    }

}
