package com.moru.backend.domain.notification.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Builder
@Getter
public class FollowedEvent {
    private final UUID senderId;
    private final UUID receiverId;
    private final LocalDateTime createdAt;
}
