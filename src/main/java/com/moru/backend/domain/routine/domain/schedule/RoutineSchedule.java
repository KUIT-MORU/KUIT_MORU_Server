package com.moru.backend.domain.routine.domain.schedule;

import com.moru.backend.domain.routine.domain.Routine;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "routine_schedule")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutineSchedule {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "routine_id", nullable = false)
    private Routine routine;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10,
            columnDefinition = "ENUM('MON', 'TUE', 'WED', 'THU', 'FRI', 'SAT', 'SUN')"
    )
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private boolean alarmEnabled;

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }
    public void setTime(LocalTime time) {
        this.time = time;
    }
    public void setAlarmEnabled(boolean alarmEnabled) {
        this.alarmEnabled = alarmEnabled;
    }
}
