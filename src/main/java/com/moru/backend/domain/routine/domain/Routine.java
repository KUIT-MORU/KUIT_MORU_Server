package com.moru.backend.domain.routine.domain;

import com.moru.backend.domain.routine.domain.meta.RoutineApp;
import com.moru.backend.domain.routine.domain.meta.RoutineTag;
import com.moru.backend.domain.user.domain.User;
import com.moru.backend.global.converter.DurationToLongConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.moru.backend.domain.routine.domain.schedule.RoutineSchedule;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "routine")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Routine {
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

    @Column(length = 10, nullable = false)
    private String title;

    @Column(columnDefinition = "tinyint(1)", nullable = false)
    private boolean isSimple;

    @Column(columnDefinition = "tinyint(1)", nullable = false)
    private boolean isUserVisible; // 사용자 생성 여부 표시 

    @Column(columnDefinition = "int unsigned default 0", nullable = false)
    private Integer likeCount;

    @Column(columnDefinition = "int unsigned default 0", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column
    @Convert(converter = DurationToLongConverter.class)
    private Duration requiredTime;

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
    @Builder.Default
    private List<RoutineTag> routineTags = new ArrayList<>();

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL)
    @Builder.Default
    private List<RoutineApp> routineApps = new ArrayList<>();

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineStep> routineSteps = new ArrayList<>();

    @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RoutineSchedule> routineSchedules = new ArrayList<>();

    /**
     * 연관관계 편의 메서드: Routine에 Step을 추가하면서, Step에도 Routine을 설정해줍니다.
     */
    public void addRoutineStep(RoutineStep routineStep) {
        this.routineSteps.add(routineStep);
        routineStep.setRoutine(this); // 자식 객체에도 부모를 설정하여 양방향 관계를 완성합니다.
    }

    /**
     * 연관관계 편의 메서드: Routine에 Schedule을 추가하면서, Schedule에도 Routine을 설정해줍니다.
     */
    public void addRoutineSchedule(RoutineSchedule routineSchedule) {
        this.routineSchedules.add(routineSchedule);
        routineSchedule.setRoutine(this); // 자식 객체에도 부모를 설정하여 양방향 관계를 완성합니다.
    }

    public void addRoutineTag(RoutineTag routineTag) {
        this.routineTags.add(routineTag);
        routineTag.setRoutine(this);
    }

    public void addRoutineApp(RoutineApp routineApp) {
        this.routineApps.add(routineApp);
        routineApp.setRoutine(this);
    }
}