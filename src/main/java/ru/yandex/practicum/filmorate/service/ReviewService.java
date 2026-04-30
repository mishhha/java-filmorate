package ru.yandex.practicum.filmorate.service;


import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewStorage storage;

    private final Map<Long, Map<Long, Integer>> reviewLikes = new HashMap<>();

    public ReviewService(ReviewStorage storage) {
        this.storage = storage;
    }

    public Review create(Review review) {
        return storage.create(review);
    }

    public Review update(Review review) {
        return storage.update(review);
    }

    public void delete(Long id) {
        storage.delete(id);
        reviewLikes.remove(id);
    }

    public Review getById(Long id) {
        return storage.getById(id);
    }

    public List<Review> getAll(Long filmId, int count) {
        return storage.getAll().stream()
                .filter(r -> filmId == null || r.getFilmId().equals(filmId))
                .sorted((a, b) -> b.getUseful() - a.getUseful())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {
        Review review = storage.getById(reviewId);

        reviewLikes.putIfAbsent(reviewId, new HashMap<>());
        Map<Long, Integer> map = reviewLikes.get(reviewId);

        Integer old = map.get(userId);

        if (old == null) {
            map.put(userId, 1);
            review.setUseful(review.getUseful() + 1);
        } else if (old == -1) {
            map.put(userId, 1);
            review.setUseful(review.getUseful() + 2);
        }
    }

    public void addDislike(Long reviewId, Long userId) {
        Review review = storage.getById(reviewId);

        reviewLikes.putIfAbsent(reviewId, new HashMap<>());
        Map<Long, Integer> map = reviewLikes.get(reviewId);

        Integer old = map.get(userId);

        if (old == null) {
            map.put(userId, -1);
            review.setUseful(review.getUseful() - 1);
        } else if (old == 1) {
            map.put(userId, -1);
            review.setUseful(review.getUseful() - 2);
        }
    }

    public void removeLike(Long reviewId, Long userId) {
        Review review = storage.getById(reviewId);

        Map<Long, Integer> map = reviewLikes.get(reviewId);
        if (map == null) return;

        Integer old = map.remove(userId);

        if (old != null && old == 1) {
            review.setUseful(review.getUseful() - 1);
        } else if (old != null && old == -1) {
            review.setUseful(review.getUseful() + 1);
        }
    }
}
