package ru.yandex.practicum.filmorate.storage.mpa;

import ru.yandex.practicum.filmorate.model.film.RatingMpa;

import java.util.List;

public interface RatingMpaStorage {

    List<RatingMpa> getAllRatingMpa();

    RatingMpa getRatingMpaById(Long id);

}
