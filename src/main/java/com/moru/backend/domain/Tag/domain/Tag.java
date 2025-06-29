package com.moru.backend.domain.Tag.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 5)
    private String name;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
