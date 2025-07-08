package com.moru.backend.domain.routine.domain;

import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.user.domain.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "routine")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 10, nullable = false)
    private String title;

    @Column(columnDefinition = "tinyint(1)", nullable = false)
    private boolean isSimple;

    @Column(columnDefinition = "tinyint(1)", nullable = false)
    private boolean isUserVisible; // 사용자 생성 여부 표시 

    @Column(columnDefinition = "int unsigned default 0", nullable = false)
    private Integer likeCount;

    @Column
    private LocalTime requiredTime;

    @Column(length = 500)
    private String imageUrl;

    @Lob
    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    private Boolean status = true;

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL)
    private List<RoutineTag> routineTags;

    @OneToMany(mappedBy = "routine")
    private List<RoutineApp> routineApps = new ArrayList<>();
}