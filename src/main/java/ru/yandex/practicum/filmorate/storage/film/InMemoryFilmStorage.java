package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.UserService;


import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

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
    public List<Film> getPopularFilms(int count, Long genreId, Integer year) {

        return films.values().stream()
                .filter(film -> genreId == null ||
                        (film.getGenres() != null &&
                                film.getGenres().stream()
                                        .anyMatch(g -> g.getId().equals(genreId))))
                .filter(film -> year == null ||
                        (film.getReleaseDate() != null &&
                                film.getReleaseDate().getYear() == year))
                .sorted(Comparator.comparingLong(Film::getLikes).reversed())
                .limit(count)
                .toList();
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
        film.addLike();
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        film.dislike();
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
        Set<Long> friendFilmList = userService.getUsersById(friendId).getLikesFilms();
        return userService.getUsersById(userId).getLikesFilms().stream().
                filter(friendFilmList::contains).map(films::get).
                filter(Objects::nonNull).sorted((film1, film2) -> (int) (film2.getLikes() - film1.getLikes()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        Comparator<Film> sortByComparator;
        switch (sortBy.toLowerCase()) {
            case "likes":
                sortByComparator = (film1, film2) -> (int) (film2.getLikes() - film1.getLikes());
                break;
            case "year":
                sortByComparator = (film1, film2) -> {
                    if (film2.getReleaseDate().isAfter(film1.getReleaseDate())) {
                        return 1;
                    } else if (film2.getReleaseDate().isBefore(film1.getReleaseDate())) {
                        return -1;
                    } else {
                        return 0;
                    }
                };
                break;
            default:
                throw new ValidationException("Тип сортировки не распознан");
        }

        return films.values().stream()
                .filter(film -> film.getDirectors().contains(directorService.getDirectorById(directorId)))
                .sorted(sortByComparator)
                .collect(Collectors.toList());
    }
}
