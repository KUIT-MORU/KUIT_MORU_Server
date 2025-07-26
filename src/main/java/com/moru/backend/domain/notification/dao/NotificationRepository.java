package com.moru.backend.domain.notification.dao;

import com.moru.backend.domain.notification.domain.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    @Query("""
        SELECT n FROM Notification n
        WHERE n.receiverId = :receiverId
            AND (
                :lastCreatedAt IS NULL OR
                n.createdAt < :lastCreatedAt OR
                (n.createdAt = :lastCreatedAt AND n.id <: lastNotificationId)
            )
        ORDER BY n.createdAt DESC, n.id DESC
    """)
    List<Notification> findNotificationByCursor(
            @Param("receiverId") UUID receiverId,
            @Param("lastCreatedAt") LocalDateTime lastCreatedAt,
            @Param("lastNotificationId") UUID lastNotificationId,
            Pageable pageable
    );

    int countByReceiverId(UUID receiverId);
}
