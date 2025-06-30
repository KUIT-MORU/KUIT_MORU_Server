package com.moru.backend.domain.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_onboarding")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserOnboarding {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @Column(nullable = false, columnDefinition = "int unsigned")
    private Integer stepCompleted;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;
}
