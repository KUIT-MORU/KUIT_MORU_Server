package com.moru.backend.domain.routine.domain;

import com.moru.backend.domain.meta.domain.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "routine_tag_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"routine_log_id", "tag_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineTagLog {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_log_id", nullable = false)
    private RoutineLog routineLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
