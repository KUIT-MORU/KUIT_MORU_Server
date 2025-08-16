package com.moru.backend.domain.routine.dao.routine;

import com.moru.backend.domain.routine.domain.Routine;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class RoutineRepositoryCustomImpl implements RoutineRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<Routine> findRoutinesOrderByUpcoming(UUID userId, Pageable pageable) {
        // 1. 정렬의 기준이 될 현재 시각과 요일 정보 가져오기
        LocalDateTime now = LocalDateTime.now();
        java.time.DayOfWeek javaCurrentDay = now.getDayOfWeek();
        String currentDayString = DayOfWeek.fromJavaDay(javaCurrentDay).name();
        LocalTime currentTime = now.toLocalTime();
        int currentDayNumeric = javaCurrentDay.getValue() - 1; // 월요일=0, ... 일요일=6

        // 2. 데이터 조회 Native SQL Query
        // 복잡한 정렬 기준(오늘의 지난 시간은 맨 뒤로, 오늘부터 요일 순서대로)을 위해 Native Query 사용
        String sql = """
            SELECT DISTINCT r.* FROM routine r
            JOIN routine_schedule rs ON r.id = rs.routine_id
            WHERE r.user_id = :userId
            AND r.status = true
            ORDER BY
                -- 1. 오늘의 지난 스케줄은 맨 뒤로 보낸다 (0: 오늘 이후 또는 다른 요일, 1: 오늘 이전)
                CASE
                    WHEN rs.day_of_week = :currentDay AND rs.time < :currentTime THEN 1
                    ELSE 0
                END ASC,
                -- 2. 오늘을 시작으로 요일 순으로 정렬한다 (모듈러 연산 활용)
                (
                    (
                        CASE rs.day_of_week
                            WHEN 'MON' THEN 0 WHEN 'TUE' THEN 1 WHEN 'WED' THEN 2
                            WHEN 'THU' THEN 3 WHEN 'FRI' THEN 4 WHEN 'SAT' THEN 5
                            ELSE 6
                        END
                        - :currentDayNumeric + 7
                    ) % 7
                ) ASC,
                -- 3. 같은 요일 내에서는 시간순으로 정렬한다
                rs.time ASC
        """;

        Query query = em.createNativeQuery(sql, Routine.class);
        query.setParameter("userId", userId);
        query.setParameter("currentDay", currentDayString);
        query.setParameter("currentTime", currentTime);
        query.setParameter("currentDayNumeric", currentDayNumeric);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<Routine> routines = query.getResultList();

        // 3. 전체 카운트 조회 Native SQL Query (페이지네이션을 위함)
        String countSql = """
            SELECT count(DISTINCT r.id) FROM routine r
            JOIN routine_schedule rs ON r.id = rs.routine_id
            WHERE r.user_id = :userId
            AND r.status = true
        """;
        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("userId", userId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(routines, pageable, total);
    }
}