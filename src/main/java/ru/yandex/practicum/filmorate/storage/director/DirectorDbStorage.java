package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.storage.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Primary
@Repository("directorDbStorage")
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private static final String FIND_ALL = """
        SELECT d.id,
               d.name
        FROM directors AS d
    """;

    private static final String FIND_DIRECTOR_BY_ID = """
        SELECT d.id,
               d.name
        FROM directors AS d
        WHERE d.id = ?
    """;

    private static final String INSERT_DIRECTOR = """
        INSERT INTO directors (name)
        VALUES (?)
    """;

    private static final String UPDATE_DIRECTOR_BY_ID = """
        UPDATE directors
        SET name = ?
        WHERE id = ?
    """;

    private static final String DELETE_DIRECTOR_BY_ID = """
        DELETE FROM directors WHERE id = ?
    """;

    private static final String CHECK_DIRECTOR_EXISTS = """
        SELECT EXISTS (SELECT 1, FROM directors WHERE id = ?)
    """;

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public List<Director> getDirectors() {
        return jdbc.query(FIND_ALL, directorRowMapper);
    }

    @Override
    public Director getDirectorById(Long id) {
        checkDirectorExists(id);
        return jdbc.queryForObject(FIND_DIRECTOR_BY_ID, directorRowMapper, id);
    }

    @Override
    public Director addDirector(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_DIRECTOR, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        director.setId(keyHolder.getKey().longValue());

        return getDirectorById(director.getId());
    }

    @Override
    public Director updateDirector(Director director) {
        checkDirectorExists(director.getId());
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UPDATE_DIRECTOR_BY_ID);
            ps.setString(1, director.getName());
            ps.setLong(2, director.getId());
            return ps;
        });

        return getDirectorById(director.getId());
    }

    @Override
    public void deleteDirectorById(Long directorId) {
        checkDirectorExists(directorId);
        jdbc.update(DELETE_DIRECTOR_BY_ID, directorId);
    }

    @Override
    public void checkDirectorExists(Long id) {
        boolean directorExist = Boolean.TRUE.equals(jdbc.queryForObject(CHECK_DIRECTOR_EXISTS, Boolean.class, id));
        if (!directorExist) {
            log.warn("Режиссёр с id {} не найден", id);
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
    }
}
