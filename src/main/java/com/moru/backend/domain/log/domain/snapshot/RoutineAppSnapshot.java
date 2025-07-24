package com.moru.backend.domain.log.domain.snapshot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Entity
@Table(name = "routine_snapshot_app")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineAppSnapshot {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_snapshot_id", nullable = false)
    private RoutineSnapshot routineSnapshot;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String packageName;
}

