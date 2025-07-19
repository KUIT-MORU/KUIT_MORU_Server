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

    @Column(nullable = false)
    private PaceGrade paceGrade;

    @Column(nullable = false)
    private Double routineCompletionRate; // 루틴 페이스의 실천률

    @Column(nullable = false)
    private Double weekdayRoutineAvgCount; // 평일 하루 평균 실천 루틴수

    @Column(nullable = false)
    private Double overallWeekdayRoutineAvgCount; // 전체 평균 평일 하루 평균 실천 루틴수

    @Column(nullable = false)
    private Double weekendRoutineAvgCount; // 주말 하루 평균 실천 루틴수

    @Column(nullable = false)
    private Double overallWeekendRoutineAvgCount; // 전체 평균 주말 하루 평균 실천 루틴 수

    @Column(nullable = false)
    private Double globalAverageRoutineCompletionRate; // 전체 사용자의 루틴 실천률 평균

    // 전체 사용자 실천률 분포 데이터를 직렬화한 JSON
    // 예시: {"0-10": 3, "10-20": 5, ...}
    @Lob
    private String completionDistributionJson;

    // 시간대별 루틴 실천 수 집계 (MORNING, AFTERNOON, NIGHT, DAWN 기준)
    // 예시: {"MORNING": 8, "AFTERNOON": 5, ...}
    @Lob
    private String routineCompletionCountByTimeSlotJson;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}