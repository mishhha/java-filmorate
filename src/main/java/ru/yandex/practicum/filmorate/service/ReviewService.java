package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage storage;
    private final UserService userService;
    private final FilmService filmService;


    private final Map<Long, Map<Long, Integer>> reviewLikes = new HashMap<>();


    public Review create(Review review) {
        validate(review);

        userService.getUsersById(review.getUserId());
        filmService.getFilmById(review.getFilmId());

        review.setUseful(0);
        return storage.create(review);
    }


    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("ReviewId is null");
        }
        getReviewOrThrow(review.getReviewId());
        validate(review);
        return storage.update(review);
    }


    public void delete(Long id) {
        getReviewOrThrow(id);
        storage.delete(id);
        reviewLikes.remove(id);
    }


    public Review getById(Long id) {
        return getReviewOrThrow(id);
    }


    public List<Review> getAll(Long filmId, int count) {
        return storage.getAll().stream()
                .filter(r -> filmId == null ||
                        (r.getFilmId() != null && r.getFilmId().equals(filmId)))
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }


    public Review addLike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);

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

        return review;
    }


    public Review addDislike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);

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

        return review;
    }


    public Review removeLike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);

        Map<Long, Integer> map = reviewLikes.get(reviewId);
        if (map == null) return review;

        Integer old = map.remove(userId);

        if (old != null && old == 1) {
            review.setUseful(review.getUseful() - 1);
        } else if (old != null && old == -1) {
            review.setUseful(review.getUseful() + 1);
        }

        return review;
    }


    public Review removeDislike(Long reviewId, Long userId) {
        Review review = getReviewOrThrow(reviewId);

        Map<Long, Integer> map = reviewLikes.get(reviewId);
        if (map == null) return review;

        Integer old = map.remove(userId);

        if (old != null && old == -1) {
            review.setUseful(review.getUseful() + 1);
        } else if (old != null && old == 1) {
            review.setUseful(review.getUseful() - 1);
        }

        return review;
    }


    private void validate(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Content is empty");
        }
        if (review.getUserId() == null) {
            throw new ValidationException("UserId is null");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("FilmId is null");
        }
        if (review.getIsPositive() == null) {
            throw new ValidationException("IsPositive is null");
        }
    }

    private Review getReviewOrThrow(Long id) {
        Review review = storage.getById(id);
        if (review == null) {
            throw new NotFoundException("Review not found");
        }
        return review;
    }
}
