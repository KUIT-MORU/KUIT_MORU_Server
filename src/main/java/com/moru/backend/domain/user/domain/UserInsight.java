package com.moru.backend.domain.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_insight")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInsight {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime insightDate;

    @Column(nullable = false)
    private Integer totalRoutineCount;

    @Column(nullable = false)
    private Integer completedRoutineCount;

    @Column(columnDefinition = "json", nullable = false)
    private String routineTimeSlotCounts;

    @Column(nullable = false)
    private float adherenceRate;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;
}