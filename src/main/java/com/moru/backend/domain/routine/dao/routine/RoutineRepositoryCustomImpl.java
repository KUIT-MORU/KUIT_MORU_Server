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

        // 2. 데이터 조회 Native SQL Query (윈도우 함수 사용)
        // 각 루틴마다 '가장 가까운 다음 스케줄'을 찾아서 그 스케줄을 기준으로 정렬합니다.
        String sql = """
            WITH ranked_schedules AS (
                             -- 1단계: 각 루틴의 스케줄에 순위를 매깁니다.
                             SELECT
                                 rs.routine_id,
                                 rs.day_of_week,
                                 rs.time,
                                 ROW_NUMBER() OVER (
                                     PARTITION BY rs.routine_id -- 각 루틴별로 파티션을 나눕니다.
                                     ORDER BY
                                         -- 정렬 기준 1: 오늘의 지난 스케줄은 맨 뒤로
                                         CASE WHEN rs.day_of_week = :currentDay AND rs.time < :currentTime THEN 1 ELSE 0 END ASC,
                                         -- 정렬 기준 2: 오늘을 시작으로 요일 순으로 정렬
                                         ( (CASE rs.day_of_week WHEN 'MON' THEN 0 WHEN 'TUE' THEN 1 WHEN 'WED' THEN 2 WHEN 'THU' THEN 3 WHEN 'FRI' THEN 4 WHEN 'SAT' THEN 5 ELSE 6 END - :currentDayNumeric + 7) % 7 ) ASC,
                                         -- 정렬 기준 3: 같은 요일 내에서는 시간순으로 정렬
                                         rs.time ASC
                                 ) as rn -- 순위(rank number)
                             FROM routine_schedule rs
                         )
                         -- 2단계: 루틴과 1등 스케줄을 조인하여 최종 정렬.
                         SELECT r.*
                         FROM routine r
                         JOIN ranked_schedules rs_ranked ON r.id = rs_ranked.routine_id
                         WHERE r.user_id = :userId
                           AND r.status = true
                           AND rs_ranked.rn = 1 -- 각 루틴의 1등 스케줄만 선택.
                         ORDER BY
                             -- 위와 동일한 정렬 기준으로 최종 순서를 결정.
                             CASE WHEN rs_ranked.day_of_week = :currentDay AND rs_ranked.time < :currentTime THEN 1 ELSE 0 END ASC,
                             ( (CASE rs_ranked.day_of_week WHEN 'MON' THEN 0 WHEN 'TUE' THEN 1 WHEN 'WED' THEN 2 WHEN 'THU' THEN 3 WHEN 'FRI' THEN 4 WHEN 'SAT' THEN 5 ELSE 6 END - :currentDayNumeric + 7) % 7 ) ASC,
                             rs_ranked.time ASC
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
        // 카운트 쿼리는 JOIN을 최소화하여 성능을 확보
        String countSql = """
            SELECT count(r.id) FROM routine r
            WHERE r.user_id = :userId
            AND r.status = true
            AND EXISTS (SELECT 1 FROM routine_schedule rs WHERE rs.routine_id = r.id) -- 스케줄이 하나라도 있는 루틴만 카운트
        """;
        Query countQuery = em.createNativeQuery(countSql);
        countQuery.setParameter("userId", userId);
        long total = ((Number) countQuery.getSingleResult()).longValue();

        return new PageImpl<>(routines, pageable, total);
    }
}