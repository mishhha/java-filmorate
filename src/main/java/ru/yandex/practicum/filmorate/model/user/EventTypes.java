package ru.yandex.practicum.filmorate.model.user;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EventTypes {
    LIKE, REVIEW, FRIEND;

    @JsonValue
    public String getValue() {
        return name();
    }
}
