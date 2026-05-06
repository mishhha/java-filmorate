

package ru.yandex.practicum.filmorate.storage.film;


import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;


@Repository("filmDbStorage")
@Primary
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    public FilmDbStorage(JdbcTemplate jdbc,
                         FilmRowMapper filmRowMapper,
                         GenreRowMapper genreRowMapper) {
        this.jdbc = jdbc;
        this.filmRowMapper = filmRowMapper;
        this.genreRowMapper = genreRowMapper;
    }

    private static final String BASE_SELECT = """
            SELECT f.id,
                   f.name,
                   f.description,
                   f.release_date,
                   f.duration,
                   m.id AS rating_id,
                   m.name AS rating_name
            FROM films f
            LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
            """;

    private static final String FIND_ALL = BASE_SELECT;

    private static final String FIND_BY_ID = BASE_SELECT + " WHERE f.id = ?";

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
            //throw new NotFoundException("Фильм не найден");
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Фильм не найден");
        }
    }

    @Override
    public Film addFilm(Film film) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

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
        }, keyHolder);

        Long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(id);


        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            for (Director d : film.getDirectors()) {
                jdbc.update(
                        "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)",
                        id,
                        d.getId()
                );
            }
        }


        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                jdbc.update(
                        "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        id,
                        g.getId()
                );
            }
        }
        System.out.println(jdbc.queryForList("SELECT * FROM films_directors"));

        return getFilmById(id);
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


        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                jdbc.update(
                        "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                        film.getId(),
                        g.getId()
                );
            }
        }

        jdbc.update("DELETE FROM films_directors WHERE film_id = ?", film.getId());

        if (film.getDirectors() != null) {
            for (Director d : film.getDirectors()) {
                jdbc.update(
                        "INSERT INTO films_directors (film_id, director_id) VALUES (?, ?)",
                        film.getId(),
                        d.getId()
                );
            }
        }

        return getFilmById(film.getId());
    }

    @Override
    public void deleteFilmById(Long filmId) {
        jdbc.update(DELETE_FILM, filmId);
    }


    @Override
    public List<Film> getPopularFilms(Integer count, Integer genreId, Integer year) {

        String sql = """
                    SELECT f.id,
                           f.name,
                           f.description,
                           f.release_date,
                           f.duration,
                           m.id AS rating_id,
                           m.name AS rating_name
                    FROM films f
                    LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                    LEFT JOIN likes l ON f.id = l.film_id
                    WHERE (? IS NULL OR EXISTS (
                            SELECT 1
                            FROM film_genres fg2
                            WHERE fg2.film_id = f.id
                              AND fg2.genre_id = ?
                    ))
                    AND (? IS NULL OR EXTRACT(YEAR FROM f.release_date) = ?)
                    GROUP BY f.id
                    ORDER BY COUNT(DISTINCT l.user_id) DESC, f.id
                    LIMIT ?
                """;

        List<Film> films = jdbc.query(sql, filmRowMapper,
                genreId, genreId,
                year, year,
                count
        );

        films.forEach(this::fillRelations);
        return films;
    }


    @Override
    public void addLike(Long filmId, Long userId) {


        jdbc.update(
                "INSERT INTO likes (film_id, user_id) VALUES (?, ?)",
                filmId, userId
        );
    }

    @Override
    public void removeLike(Long filmId, Long userId) {


        jdbc.update(
                "DELETE FROM likes WHERE film_id = ? AND user_id = ?",
                filmId, userId
        );
    }


    @Override
    public List<Film> getCommonFilms(Long userId, Long friendId) {

        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.id AS rating_id,
                       m.name AS rating_name
                FROM films f
                JOIN likes l1 ON f.id = l1.film_id AND l1.user_id = ?
                JOIN likes l2 ON f.id = l2.film_id AND l2.user_id = ?
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                """;

        List<Film> films = jdbc.query(sql, filmRowMapper, userId, friendId);
        films.forEach(this::fillRelations);
        return films;
    }


    private void fillRelations(Film film) {
        film.setGenres(loadGenres(film.getId()));
        film.setLikes(loadLikes(film.getId()));
        //film.setDirectors(loadDirectors(film.getId()));


    }

    private Set<Genre> loadGenres(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        List<Genre> genres = jdbc.query(sql, genreRowMapper, filmId);

        return new LinkedHashSet<>(genres);
    }
     /*
    private Set<Genre> loadGenres(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                """;

        return new HashSet<>(jdbc.query(sql, genreRowMapper, filmId));
    }*/


    private Set<Long> loadLikes(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return new HashSet<>(jdbc.queryForList(sql, Long.class, filmId));
    }


    private Set<Director> loadDirectors(Long filmId) {
        return new HashSet<>(jdbc.query(
                "SELECT d.id, d.name " +
                        "FROM directors d " +
                        "JOIN films_directors fd ON d.id = fd.director_id " +
                        "WHERE fd.film_id = ?",
                (rs, rowNum) -> new Director(
                        rs.getLong("id"),
                        rs.getString("name")
                ),
                filmId
        ));
    }


    @Override

    public List<Film> getDirectorFilms(Long directorId, String sortBy) {
        String sql = """
                SELECT DISTINCT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       m.id AS rating_id,
                       m.name AS rating_name
                FROM films f
                LEFT JOIN mpa_ratings m ON f.mpa_rating_id = m.id
                JOIN films_directors fd ON f.id = fd.film_id
                WHERE fd.director_id = ?
                """;


        List<Film> films = jdbc.query(sql, filmRowMapper, directorId);

        films.forEach(this::fillRelations);

        Comparator<Film> comparator;

        switch (sortBy.toLowerCase()) {

            case "likes":
                comparator = Comparator.comparingInt(Film::getLikesCount).reversed();

                break;

            case "year":
                comparator = Comparator.comparing(
                        Film::getReleaseDate,
                        Comparator.nullsLast(Comparator.naturalOrder())
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown sort: " + sortBy);
        }

        System.out.println(jdbc.queryForList("SELECT * FROM films_directors"));

        return films.stream().sorted(comparator).toList();
    }

}