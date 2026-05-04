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
import java.util.*;

@Slf4j
@Primary
@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    private static final String FIND_ALL = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count,
                   m.id AS rating_id,
                   m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            ORDER BY f.id
            """;

    private static final String FIND_FILM_BY_ID = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count,
                   f.mpa_rating_id,
                   m.id AS rating_id,
                   m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE f.id = ?
            """;

    private static final String FIND_GENRES_BY_FILM_ID = """
            SELECT g.id, g.name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            ORDER BY g.id
            """;


    private static final String FIND_TOP_FILMS_QUERY = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count,
                   m.id AS rating_id,
                   m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE (? IS NULL OR EXISTS (
                SELECT 1
                FROM film_genres fg
                WHERE fg.film_id = f.id
                  AND fg.genre_id = ?
            ))
            AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
            ORDER BY f.likes_count DESC
            LIMIT ?
            """;

    private static final String INSERT_FILM = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_FILM = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?
            """;

    private static final String DELETE_GENRES = """
            DELETE FROM film_genres WHERE film_id = ?
            """;

    private static final String INSERT_GENRE = """
            INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)
            """;

    private static final String INSERT_LIKE = """
            INSERT INTO likes (film_id, user_id) VALUES (?, ?)
            """;

    private static final String DELETE_LIKE = """
            DELETE FROM likes WHERE film_id = ? AND user_id = ?
            """;

    private static final String INC_LIKE = """
            UPDATE films SET likes_count = likes_count + 1 WHERE id = ?
            """;

    private static final String DEC_LIKE = """
            UPDATE films SET likes_count = likes_count - 1 WHERE id = ?
            """;

    private static final String DELETE_FILM = """
            DELETE FROM films WHERE id = ?
            """;

    private static final String EXISTS_FILM = """
            SELECT EXISTS (SELECT 1 FROM films WHERE id = ?)
            """;


    private static final String EXISTS_USER = """
            SELECT EXISTS (SELECT 1 FROM users WHERE id = ?)
            """;

    private static final String FIND_COMMON_FILMS = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count,
                   f.mpa_rating_id,
                   m.id AS rating_id,
                   m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            JOIN likes l ON f.id = l.film_id
            WHERE l.user_id IN (?, ?)
            GROUP BY f.id
            HAVING COUNT(*) > 1
            ORDER BY f.likes_count DESC
            """;


    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbc.query(FIND_ALL, filmRowMapper);
        films.forEach(f -> f.setGenres(getGenresByFilmId(f.getId())));
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            film.setGenres(getGenresByFilmId(id));
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {
        KeyHolder kh = new GeneratedKeyHolder();

        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());

            if (film.getRating() != null && film.getRating().getId() != null) {
                ps.setLong(5, film.getRating().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }

            return ps;
        }, kh);

        film.setId(Objects.requireNonNull(kh.getKey()).longValue());

        saveGenres(film);

        return getFilmById(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {
        jdbc.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(UPDATE_FILM);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setInt(4, film.getDuration());
            ps.setLong(5, film.getRating().getId());
            ps.setLong(6, film.getId());
            return ps;
        });

        jdbc.update(DELETE_GENRES, film.getId());
        saveGenres(film);

        return getFilmById(film.getId());
    }


    @Override
    public List<Film> getTopFilms(int count, Long genreId, Integer year) {
        return jdbc.query(
                FIND_TOP_FILMS_QUERY,
                filmRowMapper,
                genreId, genreId,
                year, year,
                count
        );
    }


    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(INSERT_LIKE, filmId, userId);
        jdbc.update(INC_LIKE, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
        jdbc.update(DEC_LIKE, filmId);
    }


    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        boolean exists = jdbc.queryForObject(EXISTS_USER, Boolean.class, userId);
        if (!exists) {
            throw new NotFoundException("Пользователь не найден");
        }

        List<Film> films = jdbc.query(FIND_COMMON_FILMS, filmRowMapper, userId, friendId);
        films.forEach(f -> f.setGenres(getGenresByFilmId(f.getId())));
        return films;
    }


    @Override
    public void deleteFilmById(Long filmId) {
        if (!jdbc.queryForObject(EXISTS_FILM, Boolean.class, filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        jdbc.update(DELETE_FILM, filmId);
    }


    private List<Genre> getGenresByFilmId(Long filmId) {
        return jdbc.query(FIND_GENRES_BY_FILM_ID, genreRowMapper, filmId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;

        Set<Long> ids = new HashSet<>();
        for (Genre g : film.getGenres()) {
            ids.add(g.getId());
        }

        for (Long id : ids) {
            jdbc.update(INSERT_GENRE, film.getId(), id);
        }
    }
}