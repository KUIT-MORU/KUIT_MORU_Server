package com.moru.backend.domain.routine.dto.request;

import com.moru.backend.domain.meta.domain.App;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RoutineAppRequest(
        @NotEmpty(message = "앱 정보 리스트는 필수이다")
        List<@Valid AppInfo> apps
        ) {
}
