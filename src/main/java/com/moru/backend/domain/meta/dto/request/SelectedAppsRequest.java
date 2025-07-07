package com.moru.backend.domain.meta.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import org.hibernate.validator.constraints.NotEmpty;

import java.util.List;

@Getter
@Schema(description = "선택된 앱들 요청")
public class SelectedAppsRequest {
    @Schema(description = "선택된 앱 패키지 이름 목록", example = "[\"com.kakao.talk\", \"com.instagram.android\"]")
    @NotEmpty(message = "선택된 앱 목록은 필수입니다.")
    @Size(max = 4, message = "선택된 앱은 최대 4개까지 가능합니다.")
    private List<String> selectedAppIds;
}