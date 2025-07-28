package com.moru.backend.domain.social.dao;

import com.moru.backend.domain.social.domain.UserFollow;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserFollowRepository extends JpaRepository<UserFollow, UUID> {
    long countByFollowerId(UUID userId); //팔로잉 수
    long countByFollowingId(UUID userID); //팔로워 수

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);
    Optional<UserFollow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    List<UserFollow> findAllByFollowerId(UUID userId);
    // 팔로워 목록 (나를 팔로우하는 사람들)
    @Query("""
        SELECT uf FROM UserFollow uf
        WHERE uf.following.id = :targetUserId
        AND (
            :lastUserId IS NULL OR
            (uf.follower.nickname > :lastNickname) OR
            (uf.follower.nickname = :lastNickname AND uf.follower.id > :lastUserId)
        )
        ORDER BY uf.follower.nickname ASC, uf.follower.id ASC
    """)
    List<UserFollow> findFollowersByCursor(
            @Param("targetUserId") UUID targetUserId,
            @Param("lastNickname") String lastNickname,
            @Param("lastUserId") UUID lastUserId,
            Pageable pageable
    );

    // 팔로잉 목록 (내가 팔로우하는 사람들)
    @Query("""
        SELECT uf FROM UserFollow uf
        WHERE uf.follower.id = :targetUserId
        AND (
            :lastUserId IS NULL OR
            (uf.following.nickname > :lastNickname) OR
            (uf.following.nickname = :lastNickname AND uf.following.id > :lastUserId)
        )
        ORDER BY uf.following.nickname ASC, uf.following.id ASC
    """)
    List<UserFollow> findFollowingsByCursor(
            @Param("targetUserId") UUID targetUserId,
            @Param("lastNickname") String lastNickname,
            @Param("lastUserId") UUID lastUserId,
            Pageable pageable
    );

    @Query("SELECT uf.follower.id FROM UserFollow uf WHERE uf.following.id = :userId")
    List<UUID> findFollowerIdsByUserId(@Param("userId") UUID userId);
}
