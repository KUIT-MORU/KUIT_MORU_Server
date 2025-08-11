package com.moru.backend.domain.social.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.domain.ActionType;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.social.dao.RoutineUserActionRepository;
import com.moru.backend.domain.social.domain.RoutineUserAction;
import com.moru.backend.domain.social.dto.RoutineImportRequest;
import com.moru.backend.domain.social.dto.ScrapCursor;
import com.moru.backend.domain.social.dto.ScrappedRoutineSummaryResponse;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.common.dto.ScrollResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.util.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScrapService {
    private final RoutineRepository routineRepository;
    private final RoutineUserActionRepository routineUserActionRepository;
    private final RoutineCloner routineCloner;
    private final S3Service s3Service;

    public Long countScrap(UUID routineId) {
        return routineUserActionRepository.countByRoutineIdAndActionType(routineId, ActionType.SCRAP);
    }

    @Transactional
    public void scrap(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        if(routine.getUser().getId().equals(user.getId())) {
            throw new CustomException(ErrorCode.SCRAP_SELF_NOT_ALLOWED);
        }

        boolean alreadyExists = routineUserActionRepository.existsByUserIdAndRoutineIdAndActionType(
                routineId, user.getId(), ActionType.SCRAP
        );

        if(alreadyExists) {
            throw new CustomException(ErrorCode.SCRAP_ALREADY_EXISTS);
        }

        RoutineUserAction routineUserAction = RoutineUserAction.builder()
                .user(user)
                .routine(routine)
                .actionType(ActionType.SCRAP)
                .build();

        routineUserActionRepository.save(routineUserAction);
    }

    @Transactional
    public void unscrap(UUID routineId, User user) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        RoutineUserAction action = routineUserActionRepository.findByUserIdAndRoutineIdAndActionType(user.getId(), routineId, ActionType.SCRAP)
                .orElseThrow(() -> new CustomException(ErrorCode.SCRAP_NOT_FOUND));

        routineUserActionRepository.delete(action);
    }

    public ScrollResponse<ScrappedRoutineSummaryResponse, ScrapCursor> getScrappedRoutine(
            User user,
            LocalDateTime lastCreatedAt, UUID lastScrapId, int limit
    ) {
        Pageable pageable = PageRequest.of(0, limit);
        List<RoutineUserAction> scraps = routineUserActionRepository.findScrapsByCursor(
                user.getId(), ActionType.SCRAP,
                lastCreatedAt, lastScrapId, pageable
        );

        List<ScrappedRoutineSummaryResponse> result = scraps.stream()
                .map(scrap 
                        -> ScrappedRoutineSummaryResponse.from(
                                scrap.getRoutine(),
                                s3Service.getImageUrl(scrap.getRoutine().getImageUrl())
                        )
                )
                .toList();

        boolean hasNext = scraps.size() == limit;
        ScrapCursor nextCursor = hasNext
                ? new ScrapCursor(scraps.getLast().getCreatedAt(), scraps.getLast().getId())
                : null;
        return ScrollResponse.of(result, hasNext, nextCursor);
    }

    @Transactional
    public void importScrappedRoutines(User user, RoutineImportRequest request) {
        for(UUID routineId : request.routineIds()) {
            Routine origin = routineRepository.findById(routineId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

            if(origin.getUser().getId().equals(user.getId())) {
                throw new CustomException(ErrorCode.IMPORT_SELF_NOT_ALLOWED);
            }

            routineCloner.cloneRoutine(origin, user);
        }
    }

    public boolean isScrapped(UUID userId, UUID routineId) {
        return routineUserActionRepository.existsByUserIdAndRoutineIdAndActionType(userId, routineId, ActionType.SCRAP);
    }
}
