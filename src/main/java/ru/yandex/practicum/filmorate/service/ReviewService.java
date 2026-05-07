package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.model.user.Event;
import ru.yandex.practicum.filmorate.model.user.EventOperations;
import ru.yandex.practicum.filmorate.model.user.EventTypes;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public Review create(Review review) {

        userStorage.getUserById(review.getUserId());
        filmStorage.getFilmById(review.getFilmId());

        review = reviewStorage.addReview(review);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType(EventTypes.REVIEW)
                .operation(EventOperations.ADD)
                .entityId(review.getReviewId())
                .build());

        return review;
    }

    public Review update(Review review) {
        if (review.getReviewId() == null) {
            throw new ValidationException("Идентификатор отзыва не может быть пустым");
        }

        Review oldReview = reviewStorage.getReviewById(review.getReviewId());

        review = reviewStorage.updateReview(review);

        boolean contentChanged = !Objects.equals(oldReview.getContent(), review.getContent());
        boolean ratingChanged = !Objects.equals(oldReview.getIsPositive(), review.getIsPositive());

        if (contentChanged || ratingChanged) {
            //Добавление события в историю
            userStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType(EventTypes.REVIEW)
                .operation(EventOperations.UPDATE)
                .entityId(review.getReviewId())
                .build());
        }

        return review;
    }

    public void delete(Long id) {
        Review review = reviewStorage.getReviewById(id);

        reviewStorage.deleteReviewById(id);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(review.getUserId())
                .eventType(EventTypes.REVIEW)
                .operation(EventOperations.REMOVE)
                .entityId(review.getReviewId())
                .build());
    }

    public Review getById(Long id) {
        return reviewStorage.getReviewById(id);
    }

    public List<Review> getAll(Long filmId, int count) {

        return reviewStorage.getReviews().stream()
                .filter(r -> filmId == null ||
                        (r.getFilmId() != null && r.getFilmId().equals(filmId)))
                .sorted(Comparator.comparingInt(Review::getUseful).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    public void addLike(Long reviewId, Long userId) {

        if (reviewStorage.isReactionExists(reviewId, userId, true)) {
            return;
        }

        reviewStorage.saveReaction(reviewId, userId, true);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.ADD)
                .entityId(reviewId)
                .build());
    }

    public void addDislike(Long reviewId, Long userId) {

        if (!reviewStorage.isReactionExists(reviewId, userId, true)) {
            return;  // Лайка нет, событие не создаём
        }

        reviewStorage.saveReaction(reviewId, userId, false);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.ADD)
                .entityId(reviewId)
                .build());
    }

    public void removeLike(Long reviewId, Long userId) {
        reviewStorage.deleteReaction(reviewId, userId);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.REMOVE)
                .entityId(reviewId)
                .build());
    }

    public void removeDislike(Long reviewId, Long userId) {
        reviewStorage.deleteReaction(reviewId, userId);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(userId)
                .eventType(EventTypes.LIKE)
                .operation(EventOperations.REMOVE)
                .entityId(reviewId)
                .build());
    }
}
