package com.moru.backend.domain.routine.domain;

import com.moru.backend.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "routine_user_action",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "routine_id", "action_type"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineUserAction {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "action_type",
            nullable = false,
            length = 10,
            columnDefinition = "ENUM('LIKE','SCRAP')"
    )
    private ActionType actionType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
