package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1985, 12, 28);
    private final HashMap<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        if(film.getName() == null) {
            throw new ValidationException("Название не может быть пустым");
        }
        if(film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if(film.getReleaseDate().isAfter(MIN_DATE_RELEASE)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if(film.getDuration().isNegative()) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        Film newFilm = new Film();
        newFilm.setId(nextIdGenerate());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        films.put(newFilm.getId(), newFilm);

        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        Film oldFilm = films.get(film.getId());
        Film newFilm = new Film();

        newFilm.setId(oldFilm.getId());

        if(film.getName() == null) {
            newFilm.setName(oldFilm.getName());
        } else {
            newFilm.setName(film.getName());
        }

        if(film.getDescription() == null) {
            newFilm.setDescription(oldFilm.getDescription());
        } else {
            newFilm.setDescription(film.getDescription());
        }

        if(film.getReleaseDate() == null) {
            newFilm.setReleaseDate(oldFilm.getReleaseDate());
        } else if(!film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            newFilm.setReleaseDate(film.getReleaseDate());
        } else {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if(film.getDuration() == null) {
            newFilm.setDuration(oldFilm.getDuration());
        } else if (film.getDuration().toMinutes() > 0) {
            newFilm.setDuration(film.getDuration());
        } else {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }

        films.put(newFilm.getId(), newFilm);

        return newFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }


    public long nextIdGenerate() {
        long nextId = films.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
