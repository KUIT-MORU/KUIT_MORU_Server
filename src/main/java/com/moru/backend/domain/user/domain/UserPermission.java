package com.moru.backend.domain.user.domain;

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
@Table(name = "user_permission")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPermission {
    @Id
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private boolean notification;

    @Column(nullable = false)
    private boolean scheduledNotification;

    @Column(nullable = false)
    private boolean overlayPermission;

    @Column(nullable = false)
    private boolean doNotDisturbControl;

    @CreatedDate
    @Column(nullable = false, updatable = false, columnDefinition = "datetime(6) default CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false, columnDefinition = "datetime(6) default CURRENT_TIMESTAMP(6) on update CURRENT_TIMESTAMP(6)")
    private LocalDateTime updatedAt;
}
