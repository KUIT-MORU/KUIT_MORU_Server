package com.moru.backend.domain.routine.domain.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
@Getter
public class RoutineCreateRequest {
    @NotBlank
    @Size(max = 10)
    private String title;

    private String imageUrl; // 선택

    @NotNull
    @Size(min = 1, max = 3)
    private List<@Size(max = 5) String> tags;

    @NotNull
    private Boolean isUserVisible;

    @Size(max = 32)
    private String description; // 선택

    @NotNull
    @Size(min = 3, max = 6) //루틴 당 스텝 개수는 최소 3개, 최대 6개 
    private List<@Valid RoutineStepRequest> steps;

    @Size(max = 4) // 연동된 앱 목록, 최대 4개 
    private List<@NotNull UUID> appIds; // 앱 ID 리스트

    @NotNull
    private Boolean isSimple;

    @NotNull
    private Boolean isPublic;
    
}
