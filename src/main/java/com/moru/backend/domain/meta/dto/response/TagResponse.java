package com.moru.backend.domain.meta.dto.response;

import com.moru.backend.domain.meta.domain.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@Schema(description = "태그 응답")
public class TagResponse {
    @Schema(description = "태그 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "태그 이름", example = "생산성")
    private String name;

    @Schema(description = "생성일시", example = "2024-01-01T00:00:00")
    private LocalDateTime createdAt;

    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}