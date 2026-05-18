package ru.yandex.practicum.filmorate.model.user;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventOperations {
    REMOVE, ADD, UPDATE;

    @JsonValue
    public String getValue() {
        return name();
    }
}
