package com.moru.backend.domain.log.domain;

import com.moru.backend.domain.routine.domain.RoutineStep;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "routine_step_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineStepLog {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_log_id", nullable = false)
    private RoutineLog routineLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_step_id", nullable = false)
    private RoutineStep routineStep;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false)
    private String note;

    @Column(nullable = false)
    private LocalTime actualTime;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(columnDefinition = "json")
    private String pausedDurations; // JSON으로 저장
}
