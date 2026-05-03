package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Primary
@Component
public class InMemoryReviewStorage implements ReviewStorage {

    private final Map<Long, Review> reviews = new HashMap<>();

    @Override
    public Review create(Review review) {
        review.setReviewId(reviews.size() + 1L);
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public Review update(Review review) {
        if (!reviews.containsKey(review.getReviewId())) {
            throw new NotFoundException("Отзыв пользователя не найден");
        }

        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public void delete(Long id) {
        if (!reviews.containsKey(id)) {
            throw new NotFoundException("Отзыв пользователя не найден");
        } else {
            reviews.remove(id);
        }
    }

    @Override
    public Review getById(Long id) {
        if (!reviews.containsKey(id)) {
            throw new NotFoundException("Отзыв пользователя не найден");
        }

        return reviews.get(id);
    }

    @Override
    public List<Review> getAll() {
        return new ArrayList<>(reviews.values());
    }

    @Override
    public void addReaction(Long reviewId, Long userId, Boolean isPositive) {
        Review review = getById(reviewId);

        if (isPositive.equals(true)) {
            review.getReactions().put(userId, (byte) 1);
        } else {
            review.getReactions().put(userId, (byte) -1);
        }
    }

    @Override
    public void deleteReaction(Long reviewId, Long userId) {
        Review review = getById(reviewId);

        if (!review.getReactions().containsKey(userId)) {
            throw new NotFoundException("Реакция пользователя не найден");
        }

        review.getReactions().remove(userId);
    }
}