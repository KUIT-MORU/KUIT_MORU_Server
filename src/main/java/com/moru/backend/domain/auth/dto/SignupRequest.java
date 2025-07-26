package com.moru.backend.domain.auth.dto;

import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.global.annotation.Password;
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
        @Password @NotBlank String password
) {}