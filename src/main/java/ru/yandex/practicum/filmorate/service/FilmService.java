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

    public FilmService (@Qualifier ("filmDbStorage") FilmStorage filmStorage,
                        @Qualifier ("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        if (film.getRating().getId() != null && (film.getRating().getId() > 5 || film.getRating().getId() <= 0)) {
            throw new NotFoundException("Рейтинга с id " + film.getRating().getId() + " не существует.");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма не может быть отрицательной.");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            log.warn(
                "Превышена длина описания {} при создании.",
                film.getDescription().length()
            );
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn(
                "Указана неверная дата релиза, при создании, дата раньше допустимого значения {}",
                film.getReleaseDate()
            );
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.getFilmById(film.getId());

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

    public List<Film> getTopFilmsByLikes(int count) {
        return filmStorage.getTopFilms(count);
    }

}
