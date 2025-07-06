package com.moru.backend.domain.routine.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineStepRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.RoutineStep;
import com.moru.backend.domain.user.dao.UserRepository;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 루틴 스텝 API 통합 테스트
 * 
 * 테스트 범위:
 * - API 엔드포인트 HTTP 요청/응답 검증
 * - 실제 데이터베이스 연동 검증
 * - 비즈니스 로직 검증 (스텝 순서 조정, 개수 제한 등)
 * - 예외 처리 검증
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoutineStepControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private RoutineStepRepository routineStepRepository;

    private MockMvc mockMvc;

    private User testUser;
    private Routine testRoutine;
    private RoutineStep testStep;
    private UUID routineId;
    private UUID stepId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        // 실제 테스트 데이터 생성 및 저장
        testUser = createAndSaveTestUser();
        testRoutine = createAndSaveTestRoutine(testUser);
        testStep = createAndSaveTestStep(testRoutine);
        routineId = testRoutine.getId();
        stepId = testStep.getId();
    }

    @Test
    @DisplayName("POST /routines/{routineId}/steps - 루틴에 스텝 추가 성공")
    void addStepToRoutine_Success() throws Exception {
        // given
        String requestJson = """
                {
                    "name": "새로운 스텝",
                    "stepOrder": 1,
                    "estimatedTime": "00:05:00"
                }
                """;

        // when & then
        mockMvc.perform(post("/routines/{routineId}/steps", routineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스텝이 성공적으로 추가되었습니다."))
                .andExpect(jsonPath("$.stepOrder").value(1));

        // 실제 DB에서 스텝이 추가되었는지 확인
        assert routineStepRepository.findByRoutineOrderByStepOrder(testRoutine).size() == 2;
    }

    @Test
    @DisplayName("POST /routines/{routineId}/steps - 존재하지 않는 루틴에 스텝 추가 실패")
    void addStepToRoutine_RoutineNotFound() throws Exception {
        // given
        UUID nonExistentRoutineId = UUID.randomUUID();
        String requestJson = """
                {
                    "name": "새로운 스텝",
                    "stepOrder": 1,
                    "estimatedTime": "00:05:00"
                }
                """;

        // when & then
        mockMvc.perform(post("/routines/{routineId}/steps", nonExistentRoutineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ROUTINE_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("POST /routines/{routineId}/steps - 스텝 개수 제한 검증 (최대 6개)")
    void addStepToRoutine_StepLimitValidation() throws Exception {
        // given - 6개의 스텝을 실제로 추가
        for (int i = 1; i <= 6; i++) {
            RoutineStep step = RoutineStep.builder()
                    .routine(testRoutine)
                    .name("스텝 " + i)
                    .stepOrder(i)
                    .estimatedTime(LocalTime.of(0, 5, 0))
                    .build();
            routineStepRepository.save(step);
        }

        // 7번째 스텝 추가 시도
        String requestJson = """
                {
                    "name": "7번째 스텝",
                    "stepOrder": 7,
                    "estimatedTime": "00:05:00"
                }
                """;

        // when & then
        mockMvc.perform(post("/routines/{routineId}/steps", routineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.STEP_OVERLOADED.name()));
    }

    @Test
    @DisplayName("GET /routines/{routineId}/steps - 스텝 목록 조회 성공")
    void getRoutineSteps_Success() throws Exception {
        // given - 실제 스텝 추가
        RoutineStep step1 = RoutineStep.builder()
                .routine(testRoutine)
                .name("첫 번째 스텝")
                .stepOrder(1)
                .estimatedTime(LocalTime.of(0, 5, 0))
                .build();
        routineStepRepository.save(step1);

        RoutineStep step2 = RoutineStep.builder()
                .routine(testRoutine)
                .name("두 번째 스텝")
                .stepOrder(2)
                .estimatedTime(LocalTime.of(0, 10, 0))
                .build();
        routineStepRepository.save(step2);

        // when & then
        mockMvc.perform(get("/routines/{routineId}/steps", routineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").value("첫 번째 스텝"))
                .andExpect(jsonPath("$[0].stepOrder").value(1))
                .andExpect(jsonPath("$[0].estimatedTime").value("00:05:00"))
                .andExpect(jsonPath("$[1].name").value("두 번째 스텝"))
                .andExpect(jsonPath("$[1].stepOrder").value(2))
                .andExpect(jsonPath("$[1].estimatedTime").value("00:10:00"));
    }

    @Test
    @DisplayName("GET /routines/{routineId}/steps - 존재하지 않는 루틴 조회 실패")
    void getRoutineSteps_RoutineNotFound() throws Exception {
        // given
        UUID nonExistentRoutineId = UUID.randomUUID();

        // when & then
        mockMvc.perform(get("/routines/{routineId}/steps", nonExistentRoutineId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ROUTINE_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("PATCH /routines/{routineId}/steps/{stepId} - 스텝 수정 성공")
    void updateStep_Success() throws Exception {
        // given
        String requestJson = """
                {
                    "name": "수정된 스텝",
                    "stepOrder": 1,
                    "estimatedTime": "00:15:00"
                }
                """;

        // when & then
        mockMvc.perform(patch("/routines/{routineId}/steps/{stepId}", routineId, stepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스텝이 성공적으로 수정되었습니다."));

        // 실제 DB에서 수정되었는지 확인
        RoutineStep updatedStep = routineStepRepository.findById(stepId).orElse(null);
        assert updatedStep != null;
        assert updatedStep.getName().equals("수정된 스텝");
        assert updatedStep.getEstimatedTime().equals(LocalTime.of(0, 15, 0));
    }

    @Test
    @DisplayName("PATCH /routines/{routineId}/steps/{stepId} - 존재하지 않는 스텝 수정 실패")
    void updateStep_StepNotFound() throws Exception {
        // given
        UUID nonExistentStepId = UUID.randomUUID();
        String requestJson = """
                {
                    "name": "수정된 스텝",
                    "stepOrder": 1,
                    "estimatedTime": "00:10:00"
                }
                """;

        // when & then
        mockMvc.perform(patch("/routines/{routineId}/steps/{stepId}", routineId, nonExistentStepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ROUTINE_STEP_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("PATCH /routines/{routineId}/steps/{stepId} - 스텝 순서 변경 및 다른 스텝들 순서 조정")
    void updateStep_OrderChange_AdjustOtherSteps() throws Exception {
        // given - 두 번째 스텝 추가
        RoutineStep step2 = RoutineStep.builder()
                .routine(testRoutine)
                .name("두 번째 스텝")
                .stepOrder(2)
                .estimatedTime(LocalTime.of(0, 10, 0))
                .build();
        routineStepRepository.save(step2);

        // 첫 번째 스텝을 두 번째 순서로 변경
        String requestJson = """
                {
                    "name": "첫 번째 스텝",
                    "stepOrder": 2,
                    "estimatedTime": "00:05:00"
                }
                """;

        // when & then
        mockMvc.perform(patch("/routines/{routineId}/steps/{stepId}", routineId, stepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스텝이 성공적으로 수정되었습니다."));

        // 실제 DB에서 순서가 변경되었는지 확인
        RoutineStep updatedStep = routineStepRepository.findById(stepId).orElse(null);
        RoutineStep otherStep = routineStepRepository.findById(step2.getId()).orElse(null);
        assert updatedStep != null && otherStep != null;
        assert updatedStep.getStepOrder() == 2;
        assert otherStep.getStepOrder() == 1; // 기존 두 번째 스텝이 첫 번째로 이동
    }

    @Test
    @DisplayName("PATCH /routines/{routineId}/steps/{stepId} - 잘못된 스텝 순서로 수정 실패")
    void updateStep_InvalidStepOrder() throws Exception {
        // given
        String requestJson = """
                {
                    "name": "수정된 스텝",
                    "stepOrder": 999,
                    "estimatedTime": "00:10:00"
                }
                """;

        // when & then
        mockMvc.perform(patch("/routines/{routineId}/steps/{stepId}", routineId, stepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.INVALID_STEP_ORDER.name()));
    }

    @Test
    @DisplayName("DELETE /routines/{routineId}/steps/{stepId} - 스텝 삭제 성공")
    void deleteStep_Success() throws Exception {
        // given - 추가 스텝 생성
        RoutineStep step2 = RoutineStep.builder()
                .routine(testRoutine)
                .name("두 번째 스텝")
                .stepOrder(2)
                .estimatedTime(LocalTime.of(0, 10, 0))
                .build();
        routineStepRepository.save(step2);

        // when & then
        mockMvc.perform(delete("/routines/{routineId}/steps/{stepId}", routineId, stepId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("스텝이 성공적으로 삭제되었습니다."));

        // 실제 DB에서 삭제되었는지 확인
        assert routineStepRepository.findById(stepId).isEmpty();
        
        // 남은 스텝의 순서가 조정되었는지 확인
        RoutineStep remainingStep = routineStepRepository.findById(step2.getId()).orElse(null);
        assert remainingStep != null;
        assert remainingStep.getStepOrder() == 1; // 순서가 1로 조정되었는지 확인
    }

    @Test
    @DisplayName("DELETE /routines/{routineId}/steps/{stepId} - 존재하지 않는 스텝 삭제 실패")
    void deleteStep_StepNotFound() throws Exception {
        // given
        UUID nonExistentStepId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/routines/{routineId}/steps/{stepId}", routineId, nonExistentStepId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ROUTINE_STEP_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("DELETE /routines/{routineId}/steps/{stepId} - 다른 루틴의 스텝 삭제 실패")
    void deleteStep_WrongRoutine() throws Exception {
        // given
        UUID otherRoutineId = UUID.randomUUID();

        // when & then
        mockMvc.perform(delete("/routines/{routineId}/steps/{stepId}", otherRoutineId, stepId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value(ErrorCode.ROUTINE_NOT_FOUND.name()));
    }

    @Test
    @DisplayName("POST /routines/{routineId}/steps - 잘못된 요청 데이터 검증")
    void addStepToRoutine_InvalidRequest() throws Exception {
        // given - 필수 필드가 누락된 요청
        String requestJson = """
                {
                    "stepOrder": 1,
                    "estimatedTime": "00:05:00"
                }
                """;

        // when & then
        mockMvc.perform(post("/routines/{routineId}/steps", routineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /routines/{routineId}/steps/{stepId} - 잘못된 시간 형식 검증")
    void updateStep_InvalidTimeFormat() throws Exception {
        // given - 잘못된 시간 형식
        String requestJson = """
                {
                    "name": "수정된 스텝",
                    "stepOrder": 1,
                    "estimatedTime": "25:70:90"
                }
                """;

        // when & then
        mockMvc.perform(patch("/routines/{routineId}/steps/{stepId}", routineId, stepId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest());
    }

    // 테스트 헬퍼 메서드들
    private User createAndSaveTestUser() {
        User user = User.builder()
                .email("test@example.com")
                .nickname("테스트유저")
                .password("password123")
                .build();
        return userRepository.save(user);
    }

    private Routine createAndSaveTestRoutine(User user) {
        Routine routine = Routine.builder()
                .user(user)
                .title("테스트 루틴")
                .isSimple(true)
                .status(true)
                .build();
        return routineRepository.save(routine);
    }

    private RoutineStep createAndSaveTestStep(Routine routine) {
        RoutineStep step = RoutineStep.builder()
                .routine(routine)
                .name("테스트 스텝")
                .stepOrder(1)
                .estimatedTime(LocalTime.of(0, 5, 0))
                .build();
        return routineStepRepository.save(step);
    }
} 