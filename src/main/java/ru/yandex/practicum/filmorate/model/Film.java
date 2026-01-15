package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    Long id;
    @NotBlank(message = "Имя фильма не может быть пустым.")
    String name;
    String description;
    LocalDate releaseDate;
    @Positive
    int duration;
}
