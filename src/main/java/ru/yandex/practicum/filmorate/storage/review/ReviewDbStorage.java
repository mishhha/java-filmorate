package ru.yandex.practicum.filmorate.storage.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Reaction;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
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
                UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?
            """;

    private static final String DELETE_REVIEW_BY_ID = """
                DELETE FROM reviews WHERE id = ?
            """;

    private static final String CHECK_REVIEW_EXISTS_BY_ID = """
                SELECT EXISTS (SELECT 1 FROM reviews WHERE id = ?)
            """;

    private static final String GET_REVIEW_BY_ID = """
                SELECT * FROM reviews WHERE id = ?
            """;

    private static final String GET_REVIEWS = """
                SELECT * FROM reviews
            """;


    private static final String GET_REACTIONS_BY_REVIEW_ID = """
                SELECT * FROM reactions WHERE review_id = ?
            """;

    private static final String GET_REACTION = """
                SELECT * FROM reactions WHERE review_id = ? AND user_id = ?
            """;

    private static final String INSERT_REACTION = """
                INSERT INTO reactions (review_id, user_id, reaction) VALUES (?, ?, ?)
            """;

    private static final String DELETE_REACTION = """
                DELETE FROM reactions WHERE review_id = ? AND user_id = ?
            """;

    private static final String CHECK_REACTION_EXISTS_BY_ID = """
                SELECT EXISTS (SELECT 1 FROM reactions WHERE review_id = ? AND user_id = ?)
            """;

    @Override
    public Review addReview(Review review) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_REVIEW, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, review.getContent());
            ps.setBoolean(2, review.getIsPositive());
            ps.setLong(3, review.getUserId());
            ps.setLong(4, review.getFilmId());
            return ps;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());

        return getReviewById(review.getReviewId());
    }

    @Override
    public Review updateReview(Review review) {
        getReviewById(review.getReviewId());

        jdbc.update(UPDATE_REVIEW,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return getReviewById(review.getReviewId());
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
            jdbc.query(GET_REACTIONS_BY_REVIEW_ID, rs -> {
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
            jdbc.query(GET_REACTIONS_BY_REVIEW_ID, rs -> {
                review.getReactions().put(rs.getLong("user_id"),
                        rs.getByte("reaction"));

            }, review.getReviewId());
        }

        return reviews;
    }

    @Override
    public Reaction getReaction(Long reviewId, Long userId) {
        try {
            //Реакции
            return jdbc.query(GET_REACTION, rs -> {
                return Reaction.builder()
                        .id(rs.getLong("id"))
                        .reviewId(rs.getLong("review_id"))
                        .userId(rs.getLong("user_id"))
                        .reaction(rs.getByte("reaction"))
                        .build();
            }, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Реакция не найден");
        }
    }

    @Override
    public Long insertReaction(Long reviewId, Long userId, Boolean isPositive) {

        try {
            deleteReaction(reviewId, userId);
        } catch (Exception e) {
            log.info("Реакция не существует");
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_REACTION, Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, reviewId);
            ps.setLong(2, userId);
            ps.setLong(3, isPositive.equals(true) ? 1 : -1);
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void deleteReaction(Long reviewId, Long userId) {
        Reaction reaction = getReaction(reviewId, userId);

        jdbc.update(DELETE_REACTION, reviewId, userId);
    }
}