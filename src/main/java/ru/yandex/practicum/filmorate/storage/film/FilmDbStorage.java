package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Primary
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private static final String FIND_ALL = """
        SELECT f.id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.likes_count,
               m.id AS rating_id,
               m.name AS rating_name
        FROM films AS f
        LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
        ORDER BY f.id
        """;

    private static final String FIND_TOP_FILMS_QUERY = """
        SELECT f.id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.likes_count,
               m.id AS rating_id,
               m.name AS rating_name
        FROM films AS f
        LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
        ORDER BY f.likes_count DESC
        LIMIT ?
        """;

    private static final String FIND_GENRES_BY_FILM_ID = """
        SELECT g.id,
               g.name
        FROM film_genres AS fg
        JOIN genres AS g ON fg.genre_id = g.id
        WHERE fg.film_id = ?
        ORDER BY g.id ASC
        """;

    private static final String FIND_DIRECTORS_BY_FILM_ID = """
        SELECT d.id,
               d.name
        FROM films_directors AS fd
        JOIN directors AS d ON fd.director_id = d.id
        WHERE fd.film_id = ?
        ORDER BY d.id ASC
        """;

    private static final String FIND_FILM_BY_ID = """
        SELECT f.id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.likes_count,
               f.mpa_rating_id,
               m.id AS rating_id,
               m.name AS rating_name
        FROM films AS f
        LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
        WHERE f.id = ?
        """;

    private static final String FIND_COMMON_FILMS = """
        SELECT f.id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.likes_count,
               f.mpa_rating_id,
               m.id AS rating_id,
               m.name AS rating_name
        FROM films AS f
        LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
        JOIN likes AS l ON f.id = l.film_id
        WHERE l.user_id IN (?, ?)
        GROUP BY f.id, f.likes_count
        HAVING COUNT(*) > 1
        ORDER BY f.likes_count DESC
        """;

    private static final String INSERT_FILM_QUERY = """
        INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)
        """;

    private static final String INSERT_GENRES_FILM = """
        INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)
        """;
    private static final String INSERT_DIRECTORS_FILM = """
        INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)
        """;

    private static final String UPDATE_FILM_QUERY = """
        UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?
        """;

    private static final String DELETE_GENRES_BY_FILM_ID = """
        DELETE FROM film_genres WHERE film_id = ?
        """;

    private static final String DELETE_DIRECTORS_BY_FILM_ID = """
        DELETE FROM films_directors WHERE film_id = ?
        """;

    private static final String INSERT_ADD_LIKE = """
        INSERT INTO likes (film_id, user_id) VALUES (?, ?)
        """;

    private static final String UPDATE_FILM_LIKES = """
        UPDATE films SET likes_count = likes_count + 1 WHERE id = ?
        """;

    private static final String DELETE_LIKE_FILM = """
        DELETE FROM likes WHERE film_id = ? AND user_id = ?
        """;

    private static final String UPDATE_DISLIKES_FILM = """
        UPDATE films SET likes_count = likes_count - 1 WHERE id = ?
        """;

    private static final String DELETE_FILM_BY_ID_QUERY = """
        DELETE FROM films WHERE id = ?
        """;

    private static final String CHECK_FILM_EXISTS_BY_ID_QUERY = """
        SELECT EXISTS (SELECT 1 FROM films WHERE id = ?)
        """;

    private static final String CHECK_USER_EXISTS_BY_ID = """
        SELECT EXISTS (SELECT 1, FROM users WHERE id = ?)
    """;

    private static final String CHECK_DIRECTOR_EXISTS_BY_ID = """
        SELECT EXISTS (SELECT 1, FROM directors WHERE id = ?)
    """;

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public void deleteFilmById(Long filmId) {
        boolean checkFilm = jdbc.queryForObject(CHECK_FILM_EXISTS_BY_ID_QUERY, Boolean.class, filmId);
        if (!checkFilm) {
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }
        jdbc.update(DELETE_FILM_BY_ID_QUERY, filmId);
    }

    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbc.query(FIND_ALL, filmRowMapper);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }
        return films;
    }

    @Override
    public List<Film> getTopFilms(int count) {
        return jdbc.query(FIND_TOP_FILMS_QUERY, filmRowMapper, count);
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
            return film;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                INSERT_FILM_QUERY, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            if (film.getRating() != null && film.getRating().getId() != null) {
                ps.setLong(5, film.getRating().getId());
            } else {
                ps.setNull(5, Types.BIGINT);  // ← Сохраняем NULL в БД
            }
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genre.getId() > 6 || genre.getId() <= 0) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " не существует");
                }
                uniqueGenreIds.add(genre.getId());
            }

            for (Long genreId : uniqueGenreIds) {
                jdbc.update(INSERT_GENRES_FILM, film.getId(), genreId);
            }
        }

        // добавление режиссёров
        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> uniqueDirectorIds = new HashSet<>();
            for (Director director : film.getDirectors()) {
                if (!jdbc.queryForObject(CHECK_DIRECTOR_EXISTS_BY_ID, Boolean.class, director.getId())) {
                    throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
                }
                uniqueDirectorIds.add(director.getId());
            }

            for (Long directorId : uniqueDirectorIds) {
                jdbc.update(INSERT_DIRECTORS_FILM, film.getId(), directorId);
            }
        }

        return getFilmById(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UPDATE_FILM_QUERY);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getRating().getId());
            ps.setLong(6, film.getId());
            return ps;
        });

        jdbc.update(DELETE_GENRES_BY_FILM_ID, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> uniqueGenreIds = new HashSet<>();
            for (Genre genre : film.getGenres()) {
                if (genre.getId() > 6 || genre.getId() <= 0) {
                    throw new NotFoundException("Жанра с id " + genre.getId() + " не существует");
                }
                uniqueGenreIds.add(genre.getId());
            }

            for (Long genreId : uniqueGenreIds) {
                jdbc.update(INSERT_GENRES_FILM, film.getId(), genreId);
            }
        }

        // обновление режиссёров
        jdbc.update(DELETE_DIRECTORS_BY_FILM_ID, film.getId());

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Long> uniqueDirectorIds = new HashSet<>();
            for (Director director : film.getDirectors()) {
                if (!jdbc.queryForObject(CHECK_DIRECTOR_EXISTS_BY_ID, Boolean.class, director.getId())) {
                    throw new NotFoundException("Режиссёр с id " + director.getId() + " не найден");
                }
                uniqueDirectorIds.add(director.getId());
            }

            for (Long directorId : uniqueDirectorIds) {
                jdbc.update(INSERT_DIRECTORS_FILM, film.getId(), directorId);
            }
        }
        return getFilmById(film.getId());
    }

    private List<Genre> getGenresByFilmId(Long filmId) {
        List<Genre> genres = jdbc.query(
            FIND_GENRES_BY_FILM_ID,
            genreRowMapper,
            filmId
        );
        return new ArrayList<>(genres);
    }

    private Set<Director> getDirectorsByFilmId(Long filmId) {
        List<Director> directors = jdbc.query(
            FIND_DIRECTORS_BY_FILM_ID,
            directorRowMapper,
            filmId
        );
        return new HashSet<>(directors);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(INSERT_ADD_LIKE, filmId, userId);
        jdbc.update(UPDATE_FILM_LIKES, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE_FILM, filmId, userId);
        jdbc.update(UPDATE_DISLIKES_FILM, filmId);
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        boolean checkUser = jdbc.queryForObject(CHECK_USER_EXISTS_BY_ID, Boolean.class, userId);
        if (!checkUser) {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        List<Film> films = jdbc.query(FIND_COMMON_FILMS, filmRowMapper, userId, friendId);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }
        return films;
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        boolean checkDirector = jdbc.queryForObject(CHECK_DIRECTOR_EXISTS_BY_ID, Boolean.class, directorId);
        if (!checkDirector) {
            throw new NotFoundException("Режиссёр с id " + directorId + " не найден");
        }

        String sortType;
        switch (sortBy.toLowerCase()) {
            case "likes":
                sortBy = "likes_count";
                sortType = "DESC";
                break;
            case "year":
                sortBy = "release_date";
                sortType = "ASC";
                break;
            default:
                throw new ValidationException("Тип сортировки не распознан");
        }

        String findDirectorFilms = String.format("""
        SELECT f.id,
               f.name,
               f.description,
               f.release_date,
               f.duration,
               f.likes_count,
               f.mpa_rating_id,
               m.id AS rating_id,
               m.name AS rating_name
        FROM films AS f
        LEFT JOIN mpa_ratings AS m ON f.mpa_rating_id = m.id
        JOIN films_directors AS fd ON f.id = fd.film_id
        WHERE fd.director_id = ?
        GROUP BY f.id, f.%s
        ORDER BY f.%s %s
        """, sortBy, sortBy, sortType);

        List<Film> films = jdbc.query(findDirectorFilms, filmRowMapper, directorId);
        for (Film film : films) {
            film.setGenres(getGenresByFilmId(film.getId()));
            film.setDirectors(getDirectorsByFilmId(film.getId()));
        }

        return films;
    }
}
