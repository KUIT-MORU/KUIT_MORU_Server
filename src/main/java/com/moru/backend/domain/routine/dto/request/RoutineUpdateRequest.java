package com.moru.backend.domain.routine.dto.request;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Builder;

@Builder
@Schema(description = "루틴 수정 요청")
public record RoutineUpdateRequest(
    @Schema(description = "루틴 제목", example = "아침 루틴")
    String title,

    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    List<String> tags,

    @Schema(description = "루틴 설명", example = "매일 아침 건강한 하루를 시작하는 루틴입니다.")
    String description,

    @Schema(description = "루틴 스텝 목록. 집중 루틴은 estimatedTime 필수, 간편 루틴은 null 또는 미포함")
    List<@Valid RoutineStepRequest> steps,

    @Schema(description = "실행시 제한되는 앱 목록(집중 루틴만 사용, 간편 루틴은 null 또는 빈 배열)")
    List<String> selectedApps,

    @Schema(description = "단순 루틴 여부(true: 간편 루틴, false: 집중 루틴)", example = "true")
    Boolean isSimple,

    @Schema(description = "루틴 표시시 사용자 표시 여부", example = "true")
    Boolean isUserVisible
) {} 