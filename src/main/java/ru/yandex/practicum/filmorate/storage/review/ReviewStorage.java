package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.List;

public interface ReviewStorage {

    Review create(Review review);

    Review update(Review review);

    void delete(Long id);

    Review getById(Long id);

    List<Review> getAll();

    void addReaction(Long reviewId, Long userId, Boolean isPositive);

    void deleteReaction(Long reviewId, Long userId);
}
