package com.moru.backend.domain.log.domain;

import com.moru.backend.domain.log.domain.snapshot.RoutineSnapshot;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.converter.DurationToLongConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "routine_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineLog {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_snapshot_id", nullable = false)
    private RoutineSnapshot routineSnapshot;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column()
    private LocalDateTime endedAt;

    @Convert(converter = DurationToLongConverter.class)
    @Column()
    private Duration totalTime;

    @Column(nullable = false)
    private boolean isSimple;

    @Column(nullable = false)
    private boolean isCompleted;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "routineLog", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineStepLog> routineStepLogs = new ArrayList<>();

    public void endLog(LocalDateTime endedAt, Duration totalTime, boolean isCompleted) {
        this.endedAt = endedAt;
        this.totalTime = totalTime;
        this.isCompleted = isCompleted;
    }
}