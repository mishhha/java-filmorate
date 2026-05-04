package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Repository("reviewDbStorage")
@RequiredArgsConstructor
public class ReviewDbStorage implements ReviewStorage {
    private final JdbcTemplate jdbc;
    private final ReviewRowMapper reviewRowMapper;

    private static final String INSERT_REVIEW = """
                INSERT INTO reviews (content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_REVIEW = """
                UPDATE reviews SET content = ?, is_positive = ?, user_id = ?, film_id = ? WHERE id = ?
            """;

    private static final String DELETE_REVIEW_BY_ID = """
                DELETE FROM reviews WHERE id = ?
            """;

    private static final String CHECK_REVIEW_EXISTS_BY_ID = """
                SELECT EXISTS (SELECT 1, FROM reviews WHERE id = ?)
            """;

    private static final String GET_REVIEW_BY_ID = """
                SELECT * FROM reviews WHERE id = ?
            """;

    private static final String GET_REVIEWS = """
                SELECT * FROM reviews
            """;

    private static final String GET_REACTIONS_BY_ID = """
                SELECT * FROM reactions WHERE review_id = ?
            """;

    private static final String MERGE_REACTION = """
                MERGE INTO reactions AS target
                USING (VALUES (?, ?, ?)) AS val (review_id, user_id, reaction)
                ON target.review_id = val.review_id AND target.user_id = val.user_id
                WHEN MATCHED THEN
                    UPDATE SET target.reaction = val.reaction
                WHEN NOT MATCHED THEN
                    INSERT (review_id, user_id, reaction) VALUES (val.review_id, val.user_id, val.reaction)
            """;

    private static final String DELETE_REACTION = """
                DELETE FROM reactions WHERE review_id = ? AND user_id = ?
            """;

    private static final String CHECK_REACTION_EXISTS_BY_ID = """
                SELECT EXISTS (SELECT 1, FROM reactions WHERE review_id = ? AND user_id = ?)
            """;

    private final Map<Long, Review> reviews = new HashMap<>();

    @Override
    public Review addReview(Review review) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, review.getContent());
            ps.setByte(2, (byte) (review.getIsPositive() ? 1 : -1));
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        review.setId(keyHolder.getKey().longValue());

        return getReviewById(review.getId());
    }

    @Override
    public Review updateReview(Review review) {
        getReviewById(review.getId());

        jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                review.getId());

        return getReviewById(review.getId());
    }

    @Override
    public void deleteReviewById(Long id) {
        boolean checkUser = jdbc.queryForObject(CHECK_REVIEW_EXISTS_BY_ID, Boolean.class, id);
        if (!checkUser) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
        jdbc.update(DELETE_REVIEW_BY_ID, id);
    }

    @Override
    public Review getReviewById(Long id) {
        try {
            //Отзыв
            Review review = jdbc.queryForObject(GET_REVIEW_BY_ID, reviewRowMapper, id);

            //Реакции
            jdbc.query(GET_REACTIONS_BY_ID, rs -> {
                review.getReactions().put(rs.getLong("user_id"),
                        rs.getByte("reaction"));
            }, id);

            return review;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Отзыв с id " + id + " не найден");
        }
    }

    @Override
    public List<Review> getReviews() {
        //Отзывы
        List<Review> reviews = jdbc.query(GET_REVIEWS, reviewRowMapper);

        for (Review review : reviews) {
            //Реакции
            jdbc.query(GET_REACTIONS_BY_ID, rs -> {
                review.getReactions().put(rs.getLong("user_id"),
                        rs.getByte("reaction"));

            });
        }

        return reviews;
    }

    @Override
    public void saveReaction(Long reviewId, Long userId, Boolean isPositive) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    MERGE_REACTION, Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, reviewId);
            ps.setLong(2, userId);
            ps.setLong(3, isPositive.equals(true) ? 1 : -1);
            return ps;
        }, keyHolder);
    }

    @Override
    public void deleteReaction(Long reviewId, Long userId) {
        boolean check = jdbc.queryForObject(CHECK_REACTION_EXISTS_BY_ID, Boolean.class, reviewId, userId);
        if (!check) {
            throw new NotFoundException(String.format("Реакция пользователя %s к отзыву %s не найдена", userId, reviewId));
        }

        jdbc.update(DELETE_REACTION, reviewId, userId);
    }
}