package com.moru.backend.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest (
        @Schema(description = "이메일", example = "test@example.com")
        @Email @NotBlank String email,

        @Schema(description = "비밀번호", example = "1234abcde")
        @NotBlank @Size(min = 8, max = 20) String password
) {}
