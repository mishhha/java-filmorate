package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.RatingMpa;
import ru.yandex.practicum.filmorate.storage.mappers.RatingMpaRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RatingMpaDbStorage implements RatingMpaStorage {

    private final JdbcTemplate jdbc;
    private final RatingMpaRowMapper ratingMpaRowMapper;

    private static final String FIND_ALL_RATINGS_MPA_QUERY = "SELECT * FROM mpa_ratings AS m ORDER BY m.ID ASC";

    private static final String FIND_RATINGS_MPA_BY_ID_QUERY = "SELECT * FROM mpa_ratings AS m WHERE id = ?";

    @Override
    public List<RatingMpa> getAllRatingMpa() {
        return jdbc.query(FIND_ALL_RATINGS_MPA_QUERY, ratingMpaRowMapper);
    }

    @Override
    public RatingMpa getRatingMpaById(Long id) {
        try {
            return jdbc.queryForObject(FIND_RATINGS_MPA_BY_ID_QUERY, ratingMpaRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг с id " + id + " не найден");
        }
    }

}
