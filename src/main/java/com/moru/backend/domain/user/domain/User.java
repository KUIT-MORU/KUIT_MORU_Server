package com.moru.backend.domain.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Setter
    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('MALE','FEMALE')")
    private Gender gender;

    @Setter
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Setter
    @Column(columnDefinition = "TEXT")
    private String bio;

    @Setter
    @Column(name = "profile_image_url", length = 2000)
    private String profileImageUrl;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(columnDefinition = "tinyint default 1", nullable = false)
    private Boolean status = true;
}
