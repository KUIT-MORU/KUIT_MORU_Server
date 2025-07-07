package com.moru.backend.domain.meta.dto;

import java.util.UUID;

import com.moru.backend.domain.meta.domain.App;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "앱 목록 응답")
public class AppResponse {
    @Schema(description = "앱 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "앱 이름", example = "앱1")
    private String name;

    @Schema(description = "앱 이미지 URL", example = "https://example.com/image.jpg")
    private String iconUrl;

    @Schema(description = "패키지 이름", example = "com.example.app")
    private String packageName;

    public static AppResponse from(App app) {
        return AppResponse.builder()
                .id(app.getId())
                .name(app.getName())
                .iconUrl(app.getIconUrl()) // 필요시
                .packageName(app.getPackageName())
                .build();
    }
}
