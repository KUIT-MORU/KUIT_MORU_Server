package com.moru.backend.domain.insight.domain;

import com.moru.backend.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_insight")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInsight {
    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Double userCompletionRate;
    private Double globalAverageCompletionRate;

    @Lob
    private String completionDistributionJson;

    @Lob
    private String averageRoutineCountByDayJson;

    @Lob
    private String routineCountByTimeSlotJson;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}