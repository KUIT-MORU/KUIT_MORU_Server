package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.user.domain.User;

@Builder
@Schema(description = "루틴 상세 응답")
public record RoutineDetailResponse(
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "루틴 제목", example = "아침 루틴")
    String title,

    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    List<String> tags,

    @Schema(description = "루틴 설명", example = "매일 아침 건강한 하루를 시작하는 루틴입니다.")
    String description,

    @Schema(description = "단순 루틴 여부(true: 간편 루틴, false: 집중 루틴)", example = "true")
    Boolean isSimple,

    @Schema(description = "루틴 표시시 사용자 표시 여부", example = "true")
    Boolean isUserVisible,

    @Schema(description = "루틴 스텝 목록(집중 루틴만 estimatedTime 값, 간편 루틴은 null)")
    List<RoutineStepDetailResponse> steps,

    @Schema(description = "연동된 앱 목록(집중 루틴만 값, 간편 루틴은 null 또는 빈 배열)")
    List<RoutineAppResponse> apps,

    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2024-01-01T09:00:00")
    LocalDateTime updatedAt,

    @Schema(description = "필요 시간(집중 루틴만 값, 간편 루틴은 null)", example = "PT50M")
    Duration requiredTime,

    @Schema(description = "루틴 소유자 여부", example = "true")
    boolean isOwner
) {
    public static RoutineDetailResponse of(
        Routine routine,
        List<RoutineTag> tags,
        List<RoutineStep> steps,
        List<RoutineApp> apps,
        User currentUser
    ) {
        boolean isOwner = routine.getUser().getId().equals(currentUser.getId());
        return new RoutineDetailResponse(
            routine.getId(),
            routine.getTitle(),
            routine.getImageUrl(),
            tags.stream().map(rt -> rt.getTag().getName()).toList(),
            routine.getContent(),
            routine.isSimple(),
            routine.isUserVisible(),
            steps.stream().map(RoutineStepDetailResponse::from).toList(),
            apps.stream()
                    .map(routineApp -> RoutineAppResponse.from(routineApp.getApp()))
                    .toList(),
            routine.getCreatedAt(),
            routine.getUpdatedAt(),
            routine.getRequiredTime(),
            isOwner
        );
    }
} 