package com.moru.backend.domain.user.dao;

import com.moru.backend.domain.user.domain.UserFavoriteTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFavoriteTagRepository extends JpaRepository<UserFavoriteTag, UUID> {
    Optional<UserFavoriteTag> findByUserIdAndTagId(UUID userId, UUID tagId);
    boolean existsByUserIdAndTagId(UUID userId, UUID tagId);
    List<UserFavoriteTag> findAllByUserId(UUID userId);
}
