package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.user.Event;
import ru.yandex.practicum.filmorate.model.user.EventOperations;
import ru.yandex.practicum.filmorate.model.user.EventTypes;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Event event = Event.builder()
                .id(resultSet.getLong("id"))
                .timestamp(resultSet.getLong("timestamp"))
                .userId(resultSet.getLong("user_id"))
                .eventType(EventTypes.valueOf(resultSet.getString("event_type")))
                .operation(EventOperations.valueOf(resultSet.getString("operation")))
                .entityId(resultSet.getLong("entity_id"))
                .build();

        return event;
    }

}
