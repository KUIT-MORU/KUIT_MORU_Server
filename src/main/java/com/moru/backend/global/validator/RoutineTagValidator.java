package com.moru.backend.global.validator;

import com.moru.backend.domain.meta.dao.TagRepository;
import com.moru.backend.domain.meta.domain.Tag;
import com.moru.backend.domain.routine.dao.RoutineTagRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RoutineTagValidator {
    private final RoutineTagRepository routineTagRepository;
    private final TagRepository tagRepository;

    public void validateBatchTagConnect(Routine routine, List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
        }

        // 요청 내 중복 체크
        Set<UUID> unique = new HashSet<>(tagIds);
        if (unique.size() != tagIds.size()) {
            throw new CustomException(ErrorCode.ALREADY_CONNECTED_TAG);
        }

        // 이미 연결된 태그 개수 확인
        List<RoutineTag> existingTags = routineTagRepository.findByRoutine(routine);
        int remain = 3 - existingTags.size();
        if (tagIds.size() > remain) {
            throw new CustomException(ErrorCode.TAG_OVERLOADED);
        }

        // 존재 여부 및 중복 연결 체크
        for (UUID tagId : tagIds) {
            Tag tag = tagRepository.findById(tagId)
                    .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
            if (routineTagRepository.existsByRoutineAndTag_Id(routine, tagId)) {
                throw new CustomException(ErrorCode.ALREADY_CONNECTED_TAG);
            }
        }
    }
}
