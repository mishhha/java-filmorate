package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;

public interface ReviewStorage {

    Review addReview(Review review);

    Review updateReview(Review review);

    void deleteReviewById(Long id);

    Review getReviewById(Long id);

    List<Review> getReviews();

    void saveReaction(Long reviewId, Long userId, Boolean isPositive);

    void deleteReaction(Long reviewId, Long userId);
}
