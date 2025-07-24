package com.moru.backend.domain.routine.domain;

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
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "routine_step")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineStep {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stepOrder;

    @Column
    @Convert(converter = DurationToLongConverter.class)
    private Duration estimatedTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    public void updateStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEstimatedTime(Duration estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public void setRoutine(Routine routine) {
        this.routine = routine;
    }
}
