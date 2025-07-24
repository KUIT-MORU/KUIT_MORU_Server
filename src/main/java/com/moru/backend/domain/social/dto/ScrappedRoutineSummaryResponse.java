package com.moru.backend.domain.social.dto;

import com.moru.backend.domain.routine.domain.Routine;

import java.util.List;
import java.util.UUID;

public record ScrappedRoutineSummaryResponse(
        UUID routineId,
        String title,
        String imageUrl,
        List<String> tagNames
) {
    public static ScrappedRoutineSummaryResponse from(Routine routine, String imageFullUrl) {
        return new ScrappedRoutineSummaryResponse(
                routine.getId(),
                routine.getTitle(),
                imageFullUrl,
                routine.getRoutineTags().stream()
                        .map(routineTag -> routineTag.getTag().getName())
                        .toList()
        );
    }
}
