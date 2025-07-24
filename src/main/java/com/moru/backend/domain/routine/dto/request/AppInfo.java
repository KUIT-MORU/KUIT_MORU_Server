package com.moru.backend.domain.routine.dto.request;

import jakarta.validation.constraints.NotNull;

public record AppInfo(
   @NotNull String appName,
   @NotNull String packageName
) {}
