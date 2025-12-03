package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final static Logger log = LoggerFactory.getLogger(FilmController.class);

    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, 12, 28);
    private final HashMap<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        if(film.getName() == null || film.getName().isBlank()) {
            log.warn("Указанное некорректное имя фильма {} при создании.", "<пусто>");
            throw new ValidationException("Название не может быть пустым");
        }
        if(film.getDescription().length() > 200) {
            log.warn("Превышена длинна описания {} при создании.", film.getDescription().length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if(film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn("Указана неверная дата релиза, при создании, дата раньше допустимого значения {}",
                        film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if(film.getDuration() <= 0) {
            log.warn("Продолжительность отрицательная {} при создании.", film.getDescription().length());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
        Film newFilm = new Film();
        newFilm.setId(nextIdGenerate());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с названием {} создан.", film.getName());
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
        } else if(film.getReleaseDate().isAfter(MIN_DATE_RELEASE)) {
            newFilm.setReleaseDate(film.getReleaseDate());
        } else {
            log.warn("Неверная дата релиза фильма {}, при обновлении, {}", film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if(film.getDuration() == 0) {
            newFilm.setDuration(oldFilm.getDuration());
        } else if (film.getDuration() > 0) {
            newFilm.setDuration(film.getDuration());
        } else {
            log.warn("Отрицательная продолжительность фильма {}, указали {} при обновлении",
                        film.getName(), film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }

        films.put(newFilm.getId(), newFilm);
            log.info("Фильм {} обновлен.", film.getName());
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
