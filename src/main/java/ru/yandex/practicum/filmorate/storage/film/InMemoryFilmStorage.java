package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;

import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j

@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Long, Film> films = new HashMap<>();

    private final UserService userService;
    private final DirectorService directorService;

    public InMemoryFilmStorage(UserService userService, DirectorService directorService) {
        this.userService = userService;
        this.directorService = directorService;
    }

    @Override
    public void deleteFilmById(Long filmId) {
        films.remove(filmId);
    }

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        if (count <= 0) {
            throw new ValidationException("Параметр count должен быть больше 0");
        }

        return films.values().stream()
                .filter(film -> genreId == null ||
                        film.getGenres().stream()
                                .anyMatch(g -> g.getId().equals(genreId)))
                .filter(film -> year == null ||
                        (film.getReleaseDate() != null &&
                                film.getReleaseDate().getYear() == year))
                .sorted(Comparator.comparingInt(Film::getLikesCount).reversed())
                .limit(count)
                .collect(Collectors.toList());
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
        log.info("Фильм {} создан.", film.getName());
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
        userService.getUsersById(userId);

        film.addLike(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUsersById(userId);

        film.removeLike(userId);
    }

    public long nextIdGenerate() {
        long nextId = films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        return ++nextId;
    }


    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        Set<Long> friendFilms = userService.getUsersById(friendId).getLikesFilms();

        return userService.getUsersById(userId).getLikesFilms().stream()
                .filter(friendFilms::contains)
                .map(films::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {

        Director director = directorService.getDirectorById(directorId);

        Comparator<Film> comparator;

        switch (sortBy.toLowerCase()) {
            case "likes":
                comparator = Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed();
                break;

            case "year":
                comparator = Comparator.comparing(Film::getReleaseDate);
                break;

            default:
                throw new ValidationException("Тип сортировки не распознан");
        }

        return films.values().stream()
                .filter(film -> film.getDirectors().contains(director))
                .sorted(comparator)
                .collect(Collectors.toList());
    }
}




