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
import ru.yandex.practicum.filmorate.exceptions.ValidationException;

import ru.yandex.practicum.filmorate.model.film.Film;

import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Slf4j
@Primary
@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {


    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;

    private static final String CHECK_USER_EXISTS =
            "SELECT EXISTS (SELECT 1 FROM users WHERE id = ?)";

    private static final String CHECK_DIRECTOR_EXISTS =
            "SELECT EXISTS (SELECT 1 FROM directors WHERE id = ?)";

    private static final String CHECK_FILM_EXISTS =
            "SELECT EXISTS (SELECT 1 FROM films WHERE id = ?)";

    private static final String INSERT_FILM = """
            INSERT INTO films (name, description, release_date, duration, mpa_rating_id)
            VALUES (?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_FILM = """
            UPDATE films
            SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ?
            WHERE id = ?
            """;

    private static final String DELETE_FILM = "DELETE FROM films WHERE id = ?";

    private static final String INSERT_LIKE =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String INC_LIKES =
            "UPDATE films SET likes_count = likes_count + 1 WHERE id = ?";

    private static final String DEC_LIKES =
            "UPDATE films SET likes_count = likes_count - 1 WHERE id = ?";

    private static final String FIND_FILM_BY_ID = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count, f.mpa_rating_id,
                   m.id AS rating_id, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            WHERE f.id = ?
            """;

    private static final String FIND_GENRES = """
            SELECT g.id, g.name
            FROM film_genres fg
            JOIN genres g ON fg.genre_id = g.id
            WHERE fg.film_id = ?
            """;

    private static final String FIND_DIRECTORS = """
            SELECT d.id, d.name
            FROM films_directors fd
            JOIN directors d ON fd.director_id = d.id
            WHERE fd.film_id = ?
            """;

    private static final String FIND_COMMON = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.likes_count, f.mpa_rating_id,
                   m.id AS rating_id, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            JOIN likes l ON f.id = l.film_id
            WHERE l.user_id IN (?, ?)
            GROUP BY f.id
            HAVING COUNT(*) > 1
            ORDER BY f.likes_count DESC
            """;

    @Override
    public void deleteFilmById(Long filmId) {
        if (!jdbc.queryForObject(CHECK_FILM_EXISTS, Boolean.class, filmId)) {
            throw new NotFoundException("Фильм не найден");
        }
        jdbc.update(DELETE_FILM, filmId);
    }

    @Override
    public List<Film> getFilms() {
        return jdbc.query("SELECT * FROM films", filmRowMapper);
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_FILM_BY_ID, filmRowMapper, id);
            fillRelations(film);
            return film;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Фильм не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {

        KeyHolder kh = new GeneratedKeyHolder();

        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(INSERT_FILM, Statement.RETURN_GENERATED_KEYS);
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
        return getFilmById(film.getId());
    }

    @Override
    public Film updateFilm(Film film) {

        jdbc.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRating() != null ? film.getRating().getId() : null,
                film.getId()
        );

        return getFilmById(film.getId());
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(INSERT_LIKE, filmId, userId);
        jdbc.update(INC_LIKES, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
        jdbc.update(DEC_LIKES, filmId);
    }


    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        StringBuilder sql = new StringBuilder("""
                SELECT f.*
                FROM films f
                """);

        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append("""
                    JOIN film_genres fg ON f.id = fg.film_id
                    """);
        }

        sql.append(" WHERE 1=1 ");

        if (genreId != null) {
            sql.append(" AND fg.genre_id = ? ");
            params.add(genreId);
        }

        if (year != null) {
            sql.append(" AND EXTRACT(YEAR FROM f.release_date) = ? ");
            params.add(year);
        }

        sql.append("""
                ORDER BY f.likes_count DESC
                LIMIT ?
                """);

        params.add(count);

        return jdbc.query(sql.toString(), filmRowMapper, params.toArray());
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {
        return jdbc.query(FIND_COMMON, filmRowMapper, userId, friendId);
    }

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {

        String order = switch (sortBy) {
            case "likes" -> "likes_count DESC";
            case "year" -> "release_date ASC";
            default -> throw new ValidationException("Unknown sort");
        };

        String sql = """
                SELECT f.* FROM films f
                JOIN films_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY %s
                """.formatted(order);

        return jdbc.query(sql, filmRowMapper, directorId);
    }

    private void fillRelations(Film film) {
        film.setGenres(new HashSet<>(jdbc.query(FIND_GENRES, genreRowMapper, film.getId())));
        film.setDirectors(new HashSet<>(jdbc.query(FIND_DIRECTORS, directorRowMapper, film.getId())));
    }
}