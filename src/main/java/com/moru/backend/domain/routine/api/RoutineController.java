package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.domain.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;

    @PostMapping
    public Object createRoutine(@Valid @RequestBody RoutineCreateRequest request) {
        // 임시로 더미 사용자 생성 (테스트용)
        User dummyUser = new User(
            UUID.randomUUID(),
            "테스트유저",
            Gender.MALE,
            1990,
            "테스트 사용자입니다.",
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
        
        return routineService.createRoutine(request, dummyUser);
    }
} 