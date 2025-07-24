package com.moru.backend.domain.routine.domain.schedule;

import com.moru.backend.domain.routine.domain.Routine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.moru.backend.domain.routine.domain.schedule.DayOfWeek;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "routine_schedule_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineScheduleHistory {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    // 어떤 루틴의 스케줄 히스토리인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    // 유효 요일
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "routine_schedule_history_days", joinColumns = @JoinColumn(name = "history_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private List<DayOfWeek> scheduledDays;

    // 이 히스토리의 적용 시작일
    @Column(nullable = false)
    private LocalDateTime effectiveStartDateTime;

    // 이 히스토리의 적용 종료일 (null이면 현재까지 유효)
    private LocalDateTime effectiveEndDateTime;

    public void endHistory(LocalDateTime endDateTime) {
        this.effectiveEndDateTime = endDateTime;
    }
}
