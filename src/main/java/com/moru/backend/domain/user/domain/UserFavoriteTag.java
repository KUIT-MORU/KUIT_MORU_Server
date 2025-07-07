package com.moru.backend.domain.user.domain;

import com.moru.backend.domain.meta.domain.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "user_favorite_tag",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "tag_id"})
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFavoriteTag {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime createdAt;
}
