package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;

@Repository("filmDbStorage")
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;
    private final DirectorRowMapper directorRowMapper;

    // ---------------- FILMS ----------------

    private static final String FIND_ALL = """
            SELECT f.id, f.name, f.description, f.release_date, f.duration,
                   f.mpa_rating_id,
                   m.id AS rating_id, m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            """;

    private static final String FIND_BY_ID = FIND_ALL + " WHERE f.id = ?";

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

    // ---------------- LIKES ----------------

    private static final String CHECK_LIKE =
            "SELECT COUNT(*) FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String INSERT_LIKE =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    private static final String FIND_LIKES =
            "SELECT user_id FROM likes WHERE film_id = ?";

    // ---------------- RELATIONS ----------------

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

    // ---------------- CRUD ----------------

    @Override
    public List<Film> getFilms() {
        List<Film> films = jdbc.query(FIND_ALL, filmRowMapper);
        films.forEach(this::fillRelations);
        return films;
    }

    @Override
    public Film getFilmById(Long id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID, filmRowMapper, id);
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

            if (film.getRating() != null) {
                ps.setLong(5, film.getRating().getId());
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            return ps;
        }, kh);

        Long filmId = Objects.requireNonNull(kh.getKey()).longValue();
        film.setId(filmId);

        // 🔥 ДОБАВЬ ЭТО

        for (Genre g : film.getGenres()) {
            jdbc.update(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    filmId, g.getId()
            );
        }

        for (Director d : film.getDirectors()) {
            jdbc.update(
                    "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)",
                    filmId, d.getId()
            );
        }

        return getFilmById(filmId);
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

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        for (Genre g : film.getGenres()) {
            jdbc.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), g.getId());
        }

        jdbc.update("DELETE FROM films_directors WHERE film_id = ?", film.getId());
        for (Director d : film.getDirectors()) {
            jdbc.update("INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)",
                    film.getId(), d.getId());
        }

        return getFilmById(film.getId());
    }

    @Override
    public void deleteFilmById(Long filmId) {
        jdbc.update(DELETE_FILM, filmId);
    }

    // ---------------- LIKES (FIXED) ----------------

    @Override
    public void addLike(Long filmId, Long userId) {

        Integer exists = jdbc.queryForObject(
                CHECK_LIKE,
                Integer.class,
                filmId, userId
        );

        if (exists != null && exists > 0) return;

        jdbc.update(INSERT_LIKE, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
    }

    // ---------------- POPULAR (FIXED) ----------------

    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        StringBuilder sql = new StringBuilder(FIND_ALL);
        List<Object> params = new ArrayList<>();

        if (genreId != null) {
            sql.append(" JOIN film_genres fg ON f.id = fg.film_id ");
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
                ORDER BY (
                    SELECT COUNT(*)
                    FROM likes l
                    WHERE l.film_id = f.id
                ) DESC
                LIMIT ?
                """);

        params.add(count);

        List<Film> films = jdbc.query(sql.toString(), filmRowMapper, params.toArray());
        films.forEach(this::fillRelations);
        return films;
    }

    // ---------------- DIRECTORS (FIXED) ----------------

    @Override
    public List<Film> getDirectorFilms(Long directorId, String sortBy) {

        String order = switch (sortBy) {
            case "likes" -> """
                    (SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id) DESC
                    """;
            case "year" -> "f.release_date ASC";
            default -> throw new ValidationException("Unknown sort type");
        };

        String sql = """
                SELECT f.*, m.id AS rating_id, m.name AS rating_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                JOIN films_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY %s
                """.formatted(order);

        List<Film> films = jdbc.query(sql, filmRowMapper, directorId);
        films.forEach(this::fillRelations);
        return films;
    }

    // ---------------- RELATIONS ----------------

    private void fillRelations(Film film) {

        film.setGenres(new HashSet<>(
                jdbc.query(FIND_GENRES, genreRowMapper, film.getId())
        ));

        film.setDirectors(new HashSet<>(
                jdbc.query(FIND_DIRECTORS, directorRowMapper, film.getId())
        ));

        film.setLikes(new HashSet<>(
                jdbc.queryForList(FIND_LIKES, Long.class, film.getId())
        ));
    }

    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {

        String sql = """
                SELECT f.*, m.id AS rating_id, m.name AS rating_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                JOIN likes l1 ON f.id = l1.film_id
                JOIN likes l2 ON f.id = l2.film_id
                WHERE l1.user_id = ?
                  AND l2.user_id = ?
                GROUP BY f.id
                """;

        List<Film> films = jdbc.query(sql, filmRowMapper, userId, friendId);
        films.forEach(this::fillRelations);
        return films;
    }
}