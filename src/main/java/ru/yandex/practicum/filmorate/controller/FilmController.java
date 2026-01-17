package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film addFilm(@RequestBody Film film) {
        return filmStorage.addFilm(film);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody Film film) {
        return filmStorage.updateFilm(film);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getFilms() {
        return filmStorage.getFilms();
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

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorHandler.ErrorResponse handlerValidation(final ValidationException e) {
        return new ErrorHandler.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorHandler.ErrorResponse handlerNotFound(final NotFoundException e) {
        return new ErrorHandler.ErrorResponse(e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorHandler.ErrorResponse handlerServerException(final Throwable e) {
        return new ErrorHandler.ErrorResponse(
            "Произошла непредвиденная ошибка."
        );
    }

}
