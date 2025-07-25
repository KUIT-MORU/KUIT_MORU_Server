package com.moru.backend.domain.notification.event;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Builder
@Getter
public class RoutineCreatedEvent {
    private final UUID senderId;
    private final UUID routineId;
}