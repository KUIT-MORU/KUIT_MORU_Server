package com.moru.backend.domain.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ScrapCursor(
        LocalDateTime createdAt,
        UUID scrapId
) {}
