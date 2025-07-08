package com.moru.backend.domain.routine.domain.search;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "search_history")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {
    @Id
    @GeneratedValue
    private UUID id;

    @JoinColumn(name = "user_id", nullable = false)
    private UUID userId;

    @Column(length = 100, nullable = false)
    private String searchKeyword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SearchType searchType;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
