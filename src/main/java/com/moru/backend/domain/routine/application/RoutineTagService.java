package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.dto.response.TagResponse;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.routine.dto.request.RoutineTagConnectRequest;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import com.moru.backend.global.validator.RoutineTagValidator;
import com.moru.backend.global.validator.RoutineValidator;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoutineTagService {
    private final RoutineTagRepository routineTagRepository;
    private final RoutineRepository routineRepository;
    private final TagRepository tagRepository;
    private final RoutineValidator routineValidator;
    private final RoutineTagValidator routineTagValidator;

    @Transactional
    public List<TagResponse> addTagsToRoutine(UUID routineId, RoutineTagConnectRequest request, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);
        List<UUID> tagIds = request.tagIds();
        routineTagValidator.validateBatchTagConnect(routine, tagIds);

        for (UUID tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId).get();
            RoutineTag routineTag = RoutineTag.builder()
                    .routine(routine)
                    .tag(tag)
                    .build();
            routineTagRepository.save(routineTag);
        }

        // 루틴에 연결된 모든 태그 반환
        List<RoutineTag> routineTags = routineTagRepository.findByRoutine(routine);
        return routineTags.stream()
                .map(routineTag -> TagResponse.from(routineTag.getTag()))
                .collect(Collectors.toList());
    }

    public List<TagResponse> getRoutineTags(UUID routineId, User currentUser) {
        Routine routine = routineValidator.validateRoutineAndUserPermission(routineId, currentUser);

        List<RoutineTag> routineTags = routineTagRepository.findByRoutine(routine);
        return routineTags.stream()
                .map(routineTag -> TagResponse.from(routineTag.getTag()))
                .collect(Collectors.toList());
    }
}
