package com.moru.backend.domain.user.dto;

import com.moru.backend.domain.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.util.UUID;

@Builder
@Schema(description = "루틴 작성자 정보")
public record AuthorInfo(
        @Schema(description = "작성자 ID")
        UUID id,

        @Schema(description = "작성자 닉네임")
        String nickname,

        @Schema(description = "작성자 프로필 이미지 URL")
        String profileImageUrl
) {
    public static AuthorInfo from(User author, String profileImageUrl) {
        return AuthorInfo.builder()
                .id(author.getId())
                .nickname(author.getNickname())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}