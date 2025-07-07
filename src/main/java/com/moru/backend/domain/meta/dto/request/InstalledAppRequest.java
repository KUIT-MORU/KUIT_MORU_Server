package com.moru.backend.domain.meta.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
@Schema(description = "설치된 앱 정보")
public class InstalledAppRequest {
    @Schema(description = "패키지 이름", example = "com.kakao.talk")
    @NotBlank(message = "패키지 이름은 필수입니다.")
    private String packageName;

    @Schema(description = "앱 이름", example = "카카오톡")
    @NotBlank(message = "앱 이름은 필수입니다.")
    private String appName;

    @Schema(description = "앱 아이콘 (Base64 인코딩)", example = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...")
    private String iconBase64;
}