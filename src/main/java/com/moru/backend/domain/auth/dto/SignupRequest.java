package com.moru.backend.domain.auth.dto;

import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.global.validator.annotation.Password;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "회원가입 요청")
public record SignupRequest (
        @Schema(description = "이메일", example = "test@example.com")
        @Email @NotBlank String email,

        @Schema(description = "비밀번호", example = "1234abcde!@")
        @Password @NotBlank String password,

        @Schema(description = "닉네임", example = "MORU")
        @NotBlank @Size(max = 10) String nickname,

        @Schema(description = "성별", example = "MALE")
        @NotNull Gender gender,

        @Schema(description = "생년월일", example = "2000-01-01")
        @NotNull @Past LocalDate birthday,

        @Schema(description = "자기소개", example = "나는 모루 유저입니다.")
        @Size(max = 100) String bio,

        @Schema(description = "프로필 이미지", example = "https://your-cdn.com/profile.jpg")
        String profileImageUrl,

        @Schema(description = "관심 태그 ID 목록")
        List<UUID> tagIds

) {}