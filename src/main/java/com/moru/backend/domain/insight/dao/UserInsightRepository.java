package com.moru.backend.domain.insight.dao;

import com.moru.backend.domain.insight.domain.UserInsight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface UserInsightRepository extends JpaRepository<UserInsight, UUID> {
    List<UserInsight> findByUpdatedAtAfter(LocalDateTime localDateTime);
}
