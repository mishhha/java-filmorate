package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class InMemoryReviewStorage implements ReviewStorage {

    private final Map<Long, Review> reviews = new HashMap<>();

    @Override
    public Review addReview(Review review) {
        review.setId(reviews.size() + 1L);
        reviews.put(review.getId(), review);
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        if (!reviews.containsKey(review.getId())) {
            throw new NotFoundException("Отзыв пользователя не найден");
        }

        reviews.put(review.getId(), review);
        return review;
    }

    @Override
    public void deleteReviewById(Long id) {
        if (!reviews.containsKey(id)) {
            throw new NotFoundException("Отзыв пользователя не найден");
        } else {
            reviews.remove(id);
        }
    }

    @Override
    public Review getReviewById(Long id) {
        if (!reviews.containsKey(id)) {
            throw new NotFoundException("Отзыв пользователя не найден");
        }

        return reviews.get(id);
    }

    @Override
    public List<Review> getReviews() {
        return new ArrayList<>(reviews.values());
    }

    @Override
    public void saveReaction(Long reviewId, Long userId, Boolean isPositive) {
        Review review = getReviewById(reviewId);

        if (isPositive.equals(true)) {
            review.getReactions().put(userId, (byte) 1);
        } else {
            review.getReactions().put(userId, (byte) -1);
        }
    }

    @Override
    public void deleteReaction(Long reviewId, Long userId) {
        Review review = getReviewById(reviewId);

        if (!review.getReactions().containsKey(userId)) {
            throw new NotFoundException("Реакция пользователя не найден");
        }

        review.getReactions().remove(userId);
    }
}