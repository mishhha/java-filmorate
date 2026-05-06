package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ErrorHandler {

    @AllArgsConstructor
    @Getter
    public static class ErrorResponse {
        String error;
    }

}
