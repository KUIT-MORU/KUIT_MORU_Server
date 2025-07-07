package com.moru.backend.domain.meta.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "설치된 앱 응답")
public class InstalledAppResponse {
    @Schema(description = "앱 ID (동적으로 생성된 앱의 경우 null)", example = "550e8400-e29b-41d4-a716-446655440000")
    private String id;

    @Schema(description = "앱 이름", example = "카카오톡")
    private String name;

    @Schema(description = "패키지 이름", example = "com.kakao.talk")
    private String packageName;

    @Schema(description = "앱 아이콘 (Base64 인코딩)", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String iconUrl;
}
