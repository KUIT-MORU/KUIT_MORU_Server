package com.moru.backend.domain.social.dao;

import com.moru.backend.domain.social.domain.UserFollow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    long countByFollowerId(UUID userId); //팔로잉 수
    long countByFollowingId(UUID userID); //팔로워 수
}
