package com.moru.backend.domain.notification.dao;

import com.moru.backend.domain.notification.domain.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Modifying(clearAutomatically = true) // clearAutomatically = true: 실행 후 JPA 캐시(영속성 컨텍스트)를 자동 초기화하여 DB 상태와 동기화한다.
    @Query("""
        UPDATE Notification n
        SET n.isRead = true
        WHERE n.receiverId = :receiverId AND n.isRead = false
    """)
    int markAllAsRead(@Param("receiverId") UUID receiverId);

    int countByReceiverIdAndIsReadFalse(UUID receiverId);
}
