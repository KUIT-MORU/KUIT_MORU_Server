package com.moru.backend.domain.user.dto;

import com.moru.backend.domain.user.domain.Gender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserProfileRequest (
        @Size(max = 10) String nickname,
        Gender gender,
        @Past LocalDate birthday,
        @Size(max = 100) String bio,
        String profileImageUrl
) {}
