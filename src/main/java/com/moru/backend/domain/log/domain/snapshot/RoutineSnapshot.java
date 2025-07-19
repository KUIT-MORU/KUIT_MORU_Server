package com.moru.backend.domain.log.domain.snapshot;

import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routine_snapshot")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineSnapshot {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private UUID originalRoutineId;

    @Column(length = 10, nullable = false)
    private String title;

    @Lob
    @Column(nullable = false, columnDefinition = "text")
    private String content;

    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private boolean isSimple;

    @Column(nullable = false)
    private boolean isUserVisible;

    @Column
    private Duration requiredTime;

    @ElementCollection(targetClass = DayOfWeek.class)
    @CollectionTable(
            name = "routine_snapshot_days",
            joinColumns = @JoinColumn(name = "routine_snapshot_id")
    )
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> scheduledDays;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 연관관계 설정
    @OneToMany(mappedBy = "routineSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineStepSnapshot> stepSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "routineSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineTagSnapshot> tagSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "routineSnapshot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoutineAppSnapshot> appSnapshots = new ArrayList<>();

}
