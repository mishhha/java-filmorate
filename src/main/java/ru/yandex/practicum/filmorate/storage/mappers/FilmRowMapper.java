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
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {

        Film film = new Film();

        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));

        film.setReleaseDate(
                rs.getObject("release_date", LocalDate.class)
        );

        film.setDuration(rs.getInt("duration"));

        long ratingId = rs.getLong("rating_id");

        if (!rs.wasNull()) {
            RatingMpa rating = new RatingMpa();
            rating.setId(ratingId);
            rating.setName(rs.getString("rating_name"));
            film.setRating(rating);
        }


        return film;
    }
}