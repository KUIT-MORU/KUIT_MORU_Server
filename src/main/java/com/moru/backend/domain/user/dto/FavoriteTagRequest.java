package com.moru.backend.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

public record FavoriteTagRequest(
        @NotEmpty(message = "tagId 목록이 비어있습니다.")
        List<UUID> tagIds
) {}
