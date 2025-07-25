package com.moru.backend.domain.notification.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class FollowedEvent {
    private final UUID receiverId;
    private final UUID senderId;
}
