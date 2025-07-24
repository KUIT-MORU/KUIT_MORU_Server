package com.moru.backend.domain.social.dto;

import java.util.List;
import java.util.UUID;

public record RoutineImportRequest(
        List<UUID> routineIds
){}
