package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.RatingMpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class FilmRowMapper implements RowMapper<Film> {

    @Override
    public Film mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(resultSet.getLong("id"));
        film.setName(resultSet.getString("name"));
        film.setDescription(resultSet.getString("description"));
        film.setReleaseDate(resultSet.getObject("release_date", LocalDate.class));
        film.setDuration(resultSet.getInt("duration"));
        film.setLikes(resultSet.getInt("likes_count"));

        Long ratingId = resultSet.getLong("rating_id");
        if (!resultSet.wasNull()) {
            RatingMpa rating = new RatingMpa();
            rating.setId(ratingId);
            rating.setName(resultSet.getString("rating_name"));
            film.setRating(rating);
        }

        return film;
    }

}
