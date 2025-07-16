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

        List<DayOfWeek> daysToCreate = new ArrayList<>();
        if (request.repeatType() != null) {
            switch (request.repeatType()) {
                case "EVERYDAY" -> daysToCreate = Arrays.asList(DayOfWeek.values());
                case "WEEKDAY" -> daysToCreate = Arrays.asList(DayOfWeek.MON, DayOfWeek.TUE, DayOfWeek.WED, DayOfWeek.THU, DayOfWeek.FRI);
                case "WEEKEND" -> daysToCreate = Arrays.asList(DayOfWeek.SAT, DayOfWeek.SUN);
                case "CUSTOM" -> daysToCreate = request.customDays();
                default -> throw new CustomException(ErrorCode.INVALID_REPEAT_TYPE);
            }
        } else {
            daysToCreate.add(request.dayOfWeek());
        }

        List<RoutineScheduleResponse> responses = new ArrayList<>();
        for (DayOfWeek day : daysToCreate) {
            if (routineScheduleRepository.existsByRoutineAndDayOfWeekAndTime(routine, day, request.time())) {
                // 이미 존재하면 건너뜀(혹은 예외)
                throw new CustomException(ErrorCode.ALREADY_EXISTS_SCHEDULE);
            }
            RoutineSchedule schedule = RoutineSchedule.builder()
                    .routine(routine)
                    .dayOfWeek(day)
                    .time(request.time())
                    .alarmEnabled(Boolean.TRUE.equals(request.alarmEnabled())) // 클라이언트에서 null or false을 보내면 -> false, true -> true
                    .build();
            RoutineSchedule savedSchedule = routineScheduleRepository.save(schedule);
            responses.add(RoutineScheduleResponse.from(savedSchedule, request.repeatType(), request.customDays()));
        }
        return responses;
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
        RoutineSchedule targetSchedule = routineScheduleRepository.findById(schId)
                .orElseThrow(() -> new CustomException(ErrorCode.ALREADY_EXISTS_SCHEDULE));

        // repeatType이 있으면 기존 스케줄(해당 루틴의 모든 스케줄) 삭제 후 새로 생성
        if (request.repeatType() != null) {
            List<RoutineSchedule> existing = routineScheduleRepository.findAllByRoutineId(routineId);
            routineScheduleRepository.deleteAll(existing);
            return createSchedule(routineId, request);
        } else {
            // 단일 스케줄 수정
            targetSchedule.setDayOfWeek(request.dayOfWeek());
            targetSchedule.setTime(request.time());
            targetSchedule.setAlarmEnabled(Boolean.TRUE.equals(request.alarmEnabled()));
            routineScheduleRepository.save(targetSchedule);
            return List.of(RoutineScheduleResponse.from(targetSchedule, null, null));
        }
    }
}

