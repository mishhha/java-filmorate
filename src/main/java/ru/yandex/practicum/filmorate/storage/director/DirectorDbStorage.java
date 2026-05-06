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

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    private static final String FIND_ALL = """
                SELECT d.id,
                       d.name
                FROM directors d
            """;

    private static final String FIND_BY_ID = """
                SELECT d.id,
                       d.name
                FROM directors d
                WHERE d.id = ?
            """;

    private static final String INSERT = """
                INSERT INTO directors (name)
                VALUES (?)
            """;

    private static final String UPDATE = """
                UPDATE directors
                SET name = ?
                WHERE id = ?
            """;

    private static final String DELETE = """
                DELETE FROM directors WHERE id = ?
            """;

    private static final String CHECK_EXISTS = """
                SELECT COUNT(*) FROM directors WHERE id = ?
            """;

    @Override
    public List<Director> getDirectors() {
        return jdbc.query(FIND_ALL, directorRowMapper);
    }

    @Override
    public Director getDirectorById(Long id) {
        checkDirectorExists(id);
        return jdbc.queryForObject(FIND_BY_ID, directorRowMapper, id);
    }

    @Override
    public Director addDirector(Director director) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT,
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Не удалось получить ID режиссёра");
        }

        director.setId(key.longValue());
        return getDirectorById(director.getId());
    }

    @Override
    public Director updateDirector(Director director) {
        checkDirectorExists(director.getId());

        jdbc.update(UPDATE,
                director.getName(),
                director.getId()
        );

        return getDirectorById(director.getId());
    }


    @Override
    public void deleteDirectorById(Long directorId) {
        checkDirectorExists(directorId);
        jdbc.update(DELETE, directorId);
    }

    @Override
    public void checkDirectorExists(Long id) {
        Integer count = jdbc.queryForObject(CHECK_EXISTS, Integer.class, id);

        if (count == null || count == 0) {
            log.warn("Режиссёр с id {} не найден", id);
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
    }
}
