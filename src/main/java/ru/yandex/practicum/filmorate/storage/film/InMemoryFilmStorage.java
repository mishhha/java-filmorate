package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Long, Film> films = new HashMap<>();

    private final UserService userService;

    public InMemoryFilmStorage(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void deleteFilmById(Long filmId) {
        films.remove(filmId);
    }

    @Override
    public List<Film> getTopFilms(int count) {
        List<Film> listFilms = getFilms();
        listFilms.sort(Comparator.comparing(Film::getLikes).reversed());
        int sizeList = listFilms.size();
        if (sizeList < count) {
            return new ArrayList<>(listFilms);
        }
        return new ArrayList<>(listFilms.subList(0,count));
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long id) {
        return films.get(id);
    }

    @Override
    public Film addFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Фильм с названием {} создан.", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        films.put(film.getId(), film);
        log.info("Фильм {} обновлен.", film.getName());
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = userService.getUsersById(userId);

        film.addLike();
        user.addLikesFilms(filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        User user = userService.getUsersById(userId);

        film.dislike();
        user.getLikesFilms().remove(filmId);
    }

    public long nextIdGenerate() {
        long nextId = films.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
