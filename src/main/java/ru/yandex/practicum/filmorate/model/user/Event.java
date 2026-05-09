package ru.yandex.practicum.filmorate.model.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Event {
    private Long eventId;
    private Long timestamp;
    private Long userId;
    private EventTypes eventType;
    private EventOperations operation;
    private Long entityId;
}
