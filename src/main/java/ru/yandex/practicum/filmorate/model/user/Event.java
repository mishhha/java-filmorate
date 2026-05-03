package ru.yandex.practicum.filmorate.model.user;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
public class Event {
    private Long id;
    private Instant timestamp;
    private Long userId;
    private EventTypes eventType;
    private EventOperations operation;
    private Long entityId;
}
