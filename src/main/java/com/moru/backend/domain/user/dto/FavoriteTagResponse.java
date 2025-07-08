package com.moru.backend.domain.user.dto;

import java.util.UUID;

public record FavoriteTagResponse (
    UUID tagId,
    String tagName
) {}
