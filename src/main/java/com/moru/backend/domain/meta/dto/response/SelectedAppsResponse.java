package com.moru.backend.domain.meta.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "선택된 앱들 응답")
public class SelectedAppsResponse {
    @Schema(description = "선택된 앱 목록")
    private List<SelectedAppResponse> selectedApps;

    @Schema(description = "선택된 앱 개수")
    private int selectedCount;

    @Schema(description = "최대 선택 가능한 앱 개수")
    private int maxCount;
}