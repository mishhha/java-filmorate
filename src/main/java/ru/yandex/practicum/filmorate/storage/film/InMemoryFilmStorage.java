package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.DirectorService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryFilmStorage")
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final UserService userService;
    private final DirectorService directorService;

    public InMemoryFilmStorage(UserService userService,
                               DirectorService directorService) {
        this.userService = userService;
        this.directorService = directorService;
    }


    @Override
    public void deleteFilmById(Long filmId) {
        films.remove(filmId);
    }

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    @Override
    public Film addFilm(Film film) {

        if (film.getId() == null) {
            film.setId(nextIdGenerate());
        }


        if (film.getGenres() != null) {
            film.setGenres(new HashSet<>(film.getGenres()));
        }

        if (film.getDirectors() == null) {
            film.setDirectors(new HashSet<>());
        }

        films.put(film.getId(), film);

        log.info("Фильм {} добавлен", film.getName());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {

        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }

        Film existing = films.get(film.getId());

        existing.setName(film.getName());
        existing.setDescription(film.getDescription());
        existing.setReleaseDate(film.getReleaseDate());
        existing.setDuration(film.getDuration());
        existing.setRating(film.getRating());

        if (film.getGenres() != null) {
            existing.setGenres(new HashSet<>(film.getGenres()));
        }

        if (film.getDirectors() != null) {
            existing.setDirectors(new HashSet<>(film.getDirectors()));
        }

        return existing;
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUsersById(userId);

        film.getLikesSet().add(userId);

    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        Film film = getFilmById(filmId);
        userService.getUsersById(userId);

        film.getLikesSet().remove(userId);

    }


    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        if (count == null || count <= 0) {
            throw new ValidationException("count должен быть > 0");
        }

        return films.values().stream()
                .filter(film -> {
                    if (genreId == null) return true;
                    if (film.getGenres() == null) return false;

                    return film.getGenres().stream()
                            .anyMatch(g -> g.getId().equals(genreId));
                })
                .filter(film -> {
                    if (year == null) return true;
                    if (film.getReleaseDate() == null) return false;

                    return film.getReleaseDate().getYear() == year;
                })
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())

                .limit(count)
                .collect(Collectors.toList());
    }


    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {

        Set<Long> userLikes = films.values().stream()
                .filter(f -> f.getLikesSet().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Set<Long> friendLikes = films.values().stream()
                .filter(f -> f.getLikesSet().contains(friendId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        return userLikes.stream()
                .filter(friendLikes::contains)
                .map(films::get)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(Film::getLikes).reversed())

                .collect(Collectors.toList());
    }


    @Override


    public List<Film> getDirectorFilms(Long directorId, String sortBy) {

        directorService.getDirectorById(directorId);

        Comparator<Film> comparator = switch (sortBy.toLowerCase()) {

            case "likes" -> Comparator.comparingInt(Film::getLikes).reversed();

            case "year" -> Comparator.comparing(
                    Film::getReleaseDate,
                    Comparator.nullsLast(Comparator.naturalOrder())
            );

            default -> throw new ValidationException("Unknown sort type");
        };

        return films.values().stream()
                .filter(film -> film.getDirectors() != null &&
                        film.getDirectors().stream()
                                .anyMatch(d -> d.getId().equals(directorId)))
                .sorted(comparator)
                .collect(Collectors.toList());
    }


    private long nextIdGenerate() {
        return films.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}