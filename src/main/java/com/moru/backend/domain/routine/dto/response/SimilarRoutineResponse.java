package com.moru.backend.domain.routine.dto.response;

import com.moru.backend.domain.routine.domain.Routine;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "유사 루틴 응답")
public record SimilarRoutineResponse(
        @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "루틴 제목", example = "아침 스트레칭")
        String title,

        @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
        String imageUrl,

        @Schema(description = "대표 태그 (첫 번째 태그)", example = "운동")
        String tag
) {
    public static SimilarRoutineResponse from(Routine routine, String imageFullUrl) {
        // Routine 엔티티의 routineTags 필드에 @OrderBy가 설정되어 있으므로, findFirst()로 첫 번째 태그를 안정적으로 가져올 수 있습니다.
        String firstTag = routine.getRoutineTags().stream()
                .findFirst()
                .map(routineTag -> routineTag.getTag().getName())
                .orElse(null); // 태그가 없는 경우 null

        return new SimilarRoutineResponse(routine.getId(), routine.getTitle(), imageFullUrl, firstTag);
    }
}