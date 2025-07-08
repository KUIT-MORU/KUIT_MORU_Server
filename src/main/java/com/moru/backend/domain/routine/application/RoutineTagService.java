package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.meta.dto.response.TagResponse;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
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

    @Transactional
    public List<TagResponse> addTagsToRoutine(UUID routineId, List<UUID> tagIds) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        List<RoutineTag> existing = routineTagRepository.findByRoutine(routine);
        if (existing.size() + tagIds.size() > 3) {
            throw new CustomException(ErrorCode.TAG_OVERLOADED);
        }

        for (UUID tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
            // 이미 링크된 태그인지 검증하기
            boolean alreadyLinked = existing.stream().anyMatch(rt -> rt.getTag().getId().equals(tagId));
            if (!alreadyLinked) {
                RoutineTag routineTag = RoutineTag.builder()
                        .routine(routine)
                        .tag(tag)
                        .build();
                routineTagRepository.save(routineTag);
            }
        }
        // 다시 현 루틴에 연결된 태그들을 반환
        List<RoutineTag> updated = routineTagRepository.findByRoutine(routine);
        return updated.stream()
                .map(rt -> TagResponse.from(rt.getTag()))
                .collect(Collectors.toList());
    }
}
