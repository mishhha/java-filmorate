package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class FilmService {

    private final InMemoryFilmStorage filmStorage;
    private final InMemoryUserStorage userStorage;

    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, 12, 28);

    public Film addFilm(Film film) {
        if (film.getName().isBlank()) {
            throw new ValidationException("Имя фильма не может быть пустым.");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма не может быть отрицательной.");
        }
        if (film.getDescription().length() > 200) {
            log.warn(
                "Превышена длина описания {} при создании.",
                film.getDescription().length()
            );
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn(
                "Указана неверная дата релиза, при создании, дата раньше допустимого значения {}",
                film.getReleaseDate()
            );
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        Film newFilm = new Film();
        newFilm.setId(filmStorage.nextIdGenerate());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        return filmStorage.addFilm(newFilm);
    }

    public Film updateFilm(Film film) {
        Film oldFilm = filmStorage.getFilmById(film.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new NotFoundException("Такой фильм не найден");
        }
        Film newFilm = new Film();

        newFilm.setId(oldFilm.getId());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        return filmStorage.updateFilm(newFilm);
    }

    public List<Film> getFilms() {
        return new ArrayList<>(filmStorage.getFilms());
    }

    public void addLike(Long idFilm) {
        Film film = filmStorage.getFilmById(idFilm);
        if (film == null) {
            throw new NotFoundException("Фильм с таким ID: " + idFilm + " не найден.");
        }
        film.addLike();
    }

    public void disLike(Long idFilm) {
        Film film = filmStorage.getFilmById(idFilm);
        if (film == null) {
            throw new NotFoundException("Фильм с таким ID: " + idFilm + " не найден.");
        }
        film.dislike();
    }

    public void userLikesFilm(Long id, Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с таким ID не найден.");
        }
        user.addLikesFilms(id);
        addLike(id);
    }

    public void userDislikesFilm(Long id, Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с таким ID не найден.");
        }
        user.getLikesFilms().remove(id);
        disLike(id);
    }

    public List<Film> getTopFilmsByLikes(int count) {
        List<Film> listFilms = filmStorage.getFilms();
        listFilms.sort(Comparator.comparing(Film::getLikes).reversed());
        int sizeList = listFilms.size();
        if (sizeList < count) {
            return new ArrayList<>(listFilms);
        }
        return new ArrayList<>(listFilms.subList(0,count));
    }

}
