package com.moru.backend.domain.routine.application;

import com.moru.backend.domain.routine.dao.RoutineRepository;
import com.moru.backend.domain.routine.dao.RoutineScheduleRepository;
import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;
import com.moru.backend.domain.routine.dto.request.RoutineScheduleRequest;
import com.moru.backend.domain.routine.dto.response.RoutineScheduleResponse;
import com.moru.backend.global.exception.CustomException;
import com.moru.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RoutineScheduleService {
    private final RoutineRepository routineRepository;
    private final RoutineScheduleRepository routineScheduleRepository;

    /**
     * 루틴 스케줄 추가
     */
    @Transactional
    public List<RoutineScheduleResponse> createSchedule(UUID routineId, RoutineScheduleRequest request) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));

        List<DayOfWeek> daysToCreate = resolveDaysToCreate(request);

        List<RoutineScheduleResponse> responses = new ArrayList<>();
        for (DayOfWeek day : daysToCreate) {
            if (routineScheduleRepository.existsByRoutineAndDayOfWeekAndTime(routine, day, request.time())) {
                throw new CustomException(ErrorCode.ALREADY_EXISTS_SCHEDULE);
            }
            RoutineSchedule schedule = RoutineSchedule.builder()
                    .routine(routine)
                    .dayOfWeek(day)
                    .time(request.time())
                    .alarmEnabled(Boolean.TRUE.equals(request.alarmEnabled()))
                    .build();
            RoutineSchedule savedSchedule = routineScheduleRepository.save(schedule);
            responses.add(RoutineScheduleResponse.from(savedSchedule, request.repeatType(), daysToCreate));
        }
        return responses;
    }

    /**
     * daysToCreate 생성 로직 분리
     */
    private List<DayOfWeek> resolveDaysToCreate(RoutineScheduleRequest request) {
        String repeatType = request.repeatType();
        if (repeatType == null) {
            throw new CustomException(ErrorCode.INVALID_REPEAT_TYPE);
        }
        return switch (repeatType) {
            case "EVERYDAY" -> Arrays.asList(DayOfWeek.values());
            case "WEEKDAY" -> Arrays.asList(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI);
            case "WEEKEND" -> Arrays.asList(DayOfWeek.SAT, DayOfWeek.SUN);
            case "CUSTOM" -> {
                List<DayOfWeek> days = request.daysToCreate();
                if (days == null || days.isEmpty()) {
                    throw new CustomException(ErrorCode.INVALID_SCHEDULE_DAY);
                }
                yield days;
            }
            default -> throw new CustomException(ErrorCode.INVALID_REPEAT_TYPE);
        };
    }

    /**
     * 루틴 스케줄 목록 조회
     */
    public List<RoutineScheduleResponse> getRoutineSchedules(UUID routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));
        List<RoutineSchedule> schedules = routineScheduleRepository.findAllByRoutineId(routineId);
        return schedules.stream()
                .map(schedule -> RoutineScheduleResponse.from(schedule, null, null))
                .toList();
    }

    /**
     * 루틴 스케줄 수정
     */
    @Transactional
    public List<RoutineScheduleResponse> updateSchedule(UUID routineId, UUID schId, RoutineScheduleRequest request) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));
        routineScheduleRepository.findById(schId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALREADY_EXISTS_SCHEDULE));

        // repeatType이 있으면 기존 스케줄(해당 루틴의 모든 스케줄) 삭제 후 새로 생성
        if (request.repeatType() != null) {
            List<RoutineSchedule> existing = routineScheduleRepository.findAllByRoutineId(routineId);
            routineScheduleRepository.deleteAll(existing);
            return createSchedule(routineId, request);
        }
        throw new CustomException(ErrorCode.INVALID_REPEAT_TYPE);
    }

    /**
     * 특정 스케줄 삭제 - 루틴에 할당된 스케쥴 초기화할때
     */
    @Transactional
    public void deleteSchedule(UUID routineId, UUID schId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));
        RoutineSchedule schedule = routineScheduleRepository.findById(schId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALREADY_EXISTS_SCHEDULE));
        // 해당 루틴에 속한 스케줄인지 검증
        if (!schedule.getRoutine().getId().equals(routine.getId())) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        routineScheduleRepository.delete(schedule);
    }

    /**
     * 루틴에 할당된 모든 스케줄 삭제(초기화)
     */
    @Transactional
    public void deleteAllSchedules(UUID routineId) {
        Routine routine = routineRepository.findById(routineId)
                .orElseThrow(() -> new CustomException(ErrorCode.ROUTINE_NOT_FOUND));
        List<RoutineSchedule> schedules = routineScheduleRepository.findAllByRoutineId(routineId);
        routineScheduleRepository.deleteAll(schedules);
    }
}

