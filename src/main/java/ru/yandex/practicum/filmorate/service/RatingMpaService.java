package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.RatingMpa;
import ru.yandex.practicum.filmorate.storage.mpa.RatingMpaStorage;

import java.util.List;

@Slf4j
@Data
@Service
public class RatingMpaService {

    private final RatingMpaStorage ratingMpaStorage;

    public List<RatingMpa> getAllRatingsMpa() {
        return ratingMpaStorage.getAllRatingMpa();
    }

    public RatingMpa getRatingMpaById(Long id) {
        return ratingMpaStorage.getRatingMpaById(id);
    }

}

