package com.moru.backend.domain.meta.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;

import java.util.List;

@Getter
@Schema(description = "설치된 앱 목록 요청")
public class InstalledAppsRequest {
    @Schema(description = "설치된 앱 목록")
    @NotEmpty(message = "설치된 앱 목록은 필수입니다.")
    @Valid
    private List<InstalledAppRequest> installedApps;
}
