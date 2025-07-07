package com.moru.backend.domain.meta.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "선택된 앱 응답")
public class SelectedAppResponse {
    @Schema(description = "패키지 이름", example = "com.kakao.talk")
    private String packageName;
}