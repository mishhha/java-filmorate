package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.RatingMpa;

import java.util.List;

@Service
public interface RatingMpaStorage {

    public List<RatingMpa> getAllRatingMpa();

    public RatingMpa getRatingMpaById(Long id);

}
