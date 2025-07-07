package com.moru.backend.domain.routine.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.List;
import java.util.UUID;

@Getter
@Schema(description = "루틴에 태그 연결 요청")
public class RoutineTagConnectRequest {
    @Schema(description = "태그 아이디들")
    private List<UUID> tagIds;
}