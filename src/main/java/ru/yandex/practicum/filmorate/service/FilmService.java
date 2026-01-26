package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
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
            return new ArrayList<>(listFilms.subList(0, sizeList));
        }
        return new ArrayList<>(listFilms.subList(0,count));
    }

}
