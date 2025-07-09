package com.moru.backend.domain.log.domain.snapshot;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "routine_snapshot_tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineTagSnapshot {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_snapshot_id", nullable = false)
    private RoutineSnapshot routineSnapshot;

    @Column(nullable = false)
    private String tagName; // tag 엔티티 참조 대신 문자열로 스냅샷
}
