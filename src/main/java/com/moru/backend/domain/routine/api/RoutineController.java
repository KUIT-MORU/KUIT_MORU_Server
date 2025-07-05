package com.moru.backend.domain.routine.api;

import com.moru.backend.domain.routine.application.RoutineService;
import com.moru.backend.domain.routine.dto.request.RoutineCreateRequest;
import com.moru.backend.domain.routine.dto.response.RoutineListResponse;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/routines")
@RequiredArgsConstructor
@Tag(name = "루틴", description = "루틴 관련 API")
public class RoutineController {
    private final RoutineService routineService;
    private final UserRepository userRepository;

    @Operation(summary = "루틴 생성", description = "새로운 루틴을 생성합니다.")
    @PostMapping
    public Object createRoutine(@Valid @RequestBody RoutineCreateRequest request) {
        // 현재 로그인된 사용자 정보 가져오기
        User currentUser = getCurrentUser();
        
        return routineService.createRoutine(request, currentUser);
    }

    @Operation(summary = "내 루틴 목록 조회", description = "현재 로그인된 사용자의 루틴 목록을 조회합니다.")
    @GetMapping
    public List<RoutineListResponse> getRoutineList() {
        // 현재 로그인된 사용자 정보 가져오기
        User currentUser = getCurrentUser();
        return routineService.getRoutineList(currentUser);
    }

    /**
     * 현재 로그인된 사용자 정보를 가져오는 메서드
     * @return 현재 로그인된 사용자
     * @throws CustomException 인증되지 않은 사용자인 경우
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
        
        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID) {
            UUID userId = (UUID) principal;
            return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        } else {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }
} 