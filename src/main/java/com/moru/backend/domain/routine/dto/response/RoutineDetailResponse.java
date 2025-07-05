package com.moru.backend.domain.routine.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "루틴 상세 응답")
public class RoutineDetailResponse {
    @Schema(description = "루틴 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "루틴 제목", example = "아침 루틴")
    private String title;

    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    private List<String> tags;

    @Schema(description = "루틴 설명", example = "매일 아침 건강한 하루를 시작하는 루틴입니다.")
    private String description;

    @Schema(description = "단순 루틴 여부(true: 간편 루틴, false: 집중 루틴)", example = "true")
    private Boolean isSimple;

    @Schema(description = "루틴 표시시 사용자 표시 여부", example = "true")
    private Boolean isUserVisible;

    @Schema(description = "루틴 스텝 목록(집중 루틴만 estimatedTime 값, 간편 루틴은 null)")
    private List<RoutineStepDetailResponse> steps;

    @Schema(description = "연동된 앱 목록(집중 루틴만 값, 간편 루틴은 null 또는 빈 배열)")
    private List<String> apps;

    @Schema(description = "생성일시", example = "2024-01-01T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시", example = "2024-01-01T09:00:00")
    private LocalDateTime updatedAt;

    @Schema(description = "필요 시간(집중 루틴만 값, 간편 루틴은 null)", example = "00:50:00")
    private String requiredTime;

    public static RoutineDetailResponse of(
        Routine routine,
        List<RoutineTag> tags,
        List<RoutineStep> steps,
        List<RoutineApp> apps
    ) {
        return RoutineDetailResponse.builder()
            .id(routine.getId())
            .title(routine.getTitle())
            .imageUrl(routine.getImageUrl())
            .tags(tags.stream().map(rt -> rt.getTag().getName()).toList())
            .description(routine.getContent())
            .isSimple(routine.isSimple())
            .isUserVisible(routine.isUserVisible())
            .steps(steps.stream().map(RoutineStepDetailResponse::from).toList())
            .apps(apps.stream().map(ra -> ra.getApp().getName()).toList())
            .createdAt(routine.getCreatedAt())
            .updatedAt(routine.getUpdatedAt())
            .requiredTime(routine.getRequiredTime() != null ? routine.getRequiredTime().toString() : null)
            .build();
    }
} 