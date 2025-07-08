package com.moru.backend.domain.routine.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer stepOrder;

    @Column
    private LocalTime estimatedTime;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 엔티티가 처음 영속화(Persist)되기 직전에 호출.
     * id가 null일 경우에만 UUID를 생성해서 채워.
     */
    @PrePersist
    public void assignIdIfNeeded() {
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
    }


    public void updateStepOrder(Integer stepOrder) {
        this.stepOrder = stepOrder;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateEstimatedTime(LocalTime estimatedTime) {
        this.estimatedTime = estimatedTime;
    }
}
