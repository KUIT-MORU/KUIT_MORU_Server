package com.moru.backend.domain.user.dao;

import com.moru.backend.domain.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    List<User> findByIdIn(List<UUID> ids);
    List<User> findAllByStatusTrue();

    @Query("""
        select u.nickname
        from User u
        where u.id = :id
    """)
    String findNicknameById(@Param("id") UUID id); // 또는 Optional<String>


    @Query("""
        select u.profileImageUrl
        from User u
        where u.id = :id
    """)
    String findProfileImageUrlById(@Param("id") UUID id);
}
