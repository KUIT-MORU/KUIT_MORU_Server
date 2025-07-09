package com.moru.backend.domain.log.domain;

import com.moru.backend.domain.log.domain.snapshot.RoutineStepSnapshot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;
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
    @JoinColumn(name = "routine_step_snapshot_id", nullable = false)
    private RoutineStepSnapshot routineStep;

    @Column(nullable = false)
    private int stepOrder;

    @Column
    private String note;

    @Column(nullable = false)
    private Duration actualTime;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column(nullable = false)
    private LocalDateTime endedAt;

    @Column(columnDefinition = "json")
    private String pausedDurations; // JSON으로 저장
}
