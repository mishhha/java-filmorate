package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public void deleteFilmById(Long filmId) {
        filmStorage.deleteFilmById(filmId);
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());
        validateFilm(film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Film getFilmById(Long idFilm) {
        return filmStorage.getFilmById(idFilm);
    }

    public void userLikesFilm(Long id, Long userId) {
        filmStorage.getFilmById(id);
        userStorage.getUserById(userId);
        filmStorage.addLike(id, userId);
    }

    public void userDislikesFilm(Long id, Long userId) {
        filmStorage.getFilmById(id);
        userStorage.getUserById(userId);
        filmStorage.removeLike(id, userId);
    }

    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {
        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }

        log.info("Получение популярных фильмов: count={}, genreId={}, year={}", count, genreId, year);

        return filmStorage.getPopularFilms(count, genreId, year);
    }

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        return filmStorage.getCommonFilms(userId, friendId);
    }


    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым.");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительной.");
        }

        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.warn("Некорректная длина описания: {}", film.getDescription());
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }

        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        if (film.getRating() != null && film.getRating().getId() != null) {
            int ratingId = Math.toIntExact(film.getRating().getId());
            if (ratingId <= 0 || ratingId > 5) {
                throw new NotFoundException("Рейтинга с id " + ratingId + " не существует.");
            }
        }
    }
}
