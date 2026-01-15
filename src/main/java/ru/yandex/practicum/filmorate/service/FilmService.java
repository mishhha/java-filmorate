package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FilmService {

    FilmStorage storage;
    UserStorage userStorage;

    @Autowired
    public FilmService(InMemoryFilmStorage storage, InMemoryUserStorage userStorage) {
        this.storage = storage;
        this.userStorage = userStorage;
    }

    public void addLike(Long idFilm) {
        Film film = storage.getFilmById(idFilm);
        if (film == null) {
            throw new NotFoundException("Фильм с таким ID: " + idFilm + " не найден.");
        }
        film.addLike();
    }

    public void disLike(Long idFilm) {
        Film film = storage.getFilmById(idFilm);
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
        List<Film> listFilms = storage.getFilms();
        listFilms.sort(Comparator.comparing(Film::getLikes).reversed());
        int sizeList = listFilms.size();
        if (sizeList < count) {
            return new ArrayList<>(listFilms.subList(0, sizeList));
        }
        return new ArrayList<>(listFilms.subList(0,count));
    }

}
