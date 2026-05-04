package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ReviewRowMapper implements RowMapper<Review> {

    @Override
    public Review mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = Review.builder()
                .reviewId(resultSet.getLong("id"))
                .content(resultSet.getString("content"))
                .isPositive(resultSet.getBoolean("is_positive"))
                .userId(resultSet.getLong("user_id"))
                .filmId(resultSet.getLong("film_id"))
                .build();

        return review;
    }

}
