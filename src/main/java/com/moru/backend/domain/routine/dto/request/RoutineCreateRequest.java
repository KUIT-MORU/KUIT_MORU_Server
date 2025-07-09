package com.moru.backend.domain.routine.dto.request;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
@Schema(description = "루틴 생성 요청")
public record RoutineCreateRequest(
    @Schema(description = "루틴 제목", example = "아침 루틴")
    @NotBlank
    @Size(max = 10)
    String title,

    @Schema(description = "루틴 이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl, // 선택

    @Schema(description = "루틴 태그 목록", example = "[\"운동\", \"건강\", \"생산성\"]")
    @NotNull
    @Size(min = 1, max = 3)
    List<@Size(max = 5) String> tags,

    @Schema(description = "루틴 설명", example = "매일 아침 건강한 하루를 시작하는 루틴입니다.")
    @Size(max = 32)
    String description, // 선택

    @Schema(
        description = "루틴 스텝 목록. 집중 루틴은 estimatedTime 필수, 간편 루틴은 null 또는 미포함",
        example = "[{\"name\": \"물 마시기\", \"stepOrder\": 1, \"estimatedTime\": \"00:05:00\"}, {\"name\": \"아침 운동\", \"stepOrder\": 2, \"estimatedTime\": \"00:30:00\"}, {\"name\": \"아침 먹기\", \"stepOrder\": 3, \"estimatedTime\": \"00:15:00\"}]"
    )
    @NotNull
    @Size(min = 3, max = 6) //루틴 당 스텝 개수는 최소 3개, 최대 6개 
    List<@Valid RoutineStepRequest> steps,

    @Schema(
        description = "실행시 제한되는 앱 목록(집중 루틴만 사용, 간편 루틴은 null 또는 빈 배열)",
            example = "[\"com.kakao.talk\", \"com.instagram.android\"]"
    )
    @Size(max = 4) // 연동된 앱 목록, 최대 4개 
    List<@NotNull String> selectedApps, // 앱 ID 리스트

    @Schema(description = "단순 루틴 여부(true: 간편 루틴, false: 집중 루틴)", example = "true")
    @NotNull
    Boolean isSimple,

    @Schema(description = "루틴 표시시 사용자 표시 여부", example = "true")
    @NotNull
    Boolean isUserVisible
) {}
