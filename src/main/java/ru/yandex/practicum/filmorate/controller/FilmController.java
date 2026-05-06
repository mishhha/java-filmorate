package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;
@Slf4j
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @DeleteMapping("/{filmId}")
    public void deleteFilmById(@PathVariable Long filmId) {
        filmService.deleteFilmById(filmId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@Valid @RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable Long id) {
        return filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        filmService.userLikesFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void disLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        filmService.userDislikesFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> topFilmsByLikes(
            @RequestParam(defaultValue = "10") @PositiveOrZero int count,
            @RequestParam(required = false) Integer genreId,
            @RequestParam(required = false) Integer year
    ) {
        return filmService.getTopFilmsByLikes(count, genreId, year);
    }
}