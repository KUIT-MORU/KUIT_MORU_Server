package com.moru.backend.domain.notification.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class RoutineCreatedEvent {
    private final UUID routineId;
    private final UUID senderId;
}
