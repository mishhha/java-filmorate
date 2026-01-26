package ru.yandex.practicum.filmorate.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@Data
@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        return filmService.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilms() {
        return filmService.getFilms();
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id <= 0 || userId <= 0) {
            throw new ValidationException(
                "Передаваемые значения должны быть положительными. " +
                    "<" + id + "> " +
                    "<" + userId + ">"
            );
        }
        filmService.userLikesFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void disLike(@PathVariable Long id, @PathVariable Long userId) {
        if (id <= 0 || userId <= 0) {
            throw new ValidationException(
                "Передаваемые значения должны быть положительными. " +
                    "<" + id + "> " +
                    "<" + userId + ">"
            );
        }
        filmService.userDislikesFilm(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> topFilmsByLikes(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ValidationException(
                "Передаваемые значения должны быть положительными. " +
                    "<" + count + ">"
            );
        }
        return filmService.getTopFilmsByLikes(count);
    }

}
