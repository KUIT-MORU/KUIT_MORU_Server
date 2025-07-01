package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.Gender;
import com.moru.backend.domain.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/routines")
@RequiredArgsConstructor
public class RoutineController {
    private final RoutineService routineService;
    private final UserRepository userRepository;

    @PostMapping
    public Object createRoutine(@Valid @RequestBody RoutineCreateRequest request) {
        // 임시로 더미 사용자 생성 (테스트용)
        User dummyUser = new User(
            UUID.randomUUID(),
            "test@example.com",
            "password123",
            "테스트유저",
            Gender.MALE,
            LocalDate.of(1990, 1, 1),
            "테스트 사용자입니다.",
            null,
            LocalDateTime.now(),
            LocalDateTime.now(),
            true
        );
        
        // 더미 사용자를 데이터베이스에 저장
        User savedUser = userRepository.save(dummyUser);
        
        return routineService.createRoutine(request, savedUser);
    }
} 