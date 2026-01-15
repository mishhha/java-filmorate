package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
public class Film {
    Long id;
    String name;
    String description;
    LocalDate releaseDate;
    int duration;
    long likes;

    public void addLike() {
        ++likes;
    }

    public void dislike() {
        --likes;
    }
}
