package com.moru.backend.domain.log.domain.snapshot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.util.UUID;

@Entity
@Table(name = "routine_step_snapshot")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineStepSnapshot {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_snapshot_id", nullable = false)
    private RoutineSnapshot routineSnapshot;

    @Column(nullable = false)
    private int stepOrder;

    @Column(nullable = false)
    private String name;

    @Column
    private Duration estimatedTime; // null 허용 (isSimple이면 없어질 수 있음)
}
