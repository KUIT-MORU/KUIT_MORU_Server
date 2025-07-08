package com.moru.backend.domain.routine.dto.response;


import com.moru.backend.domain.meta.domain.App;
import com.moru.backend.domain.routine.domain.Routine;
import io.swagger.v3.oas.annotations.media.Schema;

public record RoutineAppResponse(
        @Schema(description = "앱 패키지명", example = "com.google.android.youtube")
        String packageName,

        @Schema(description = "앱 이름", example = "Youtube")
        String name
) {
    public static RoutineAppResponse from(App app) {
        return new RoutineAppResponse(
                app.getPackageName(),
                app.getName()
        );
    }
}