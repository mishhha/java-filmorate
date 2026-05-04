package ru.yandex.practicum.filmorate.controller;


import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.review.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService service;

    @PostMapping
    public Review create(@RequestBody Review review) {
        return service.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return service.update(review);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable @PositiveOrZero Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    public Review getById(@PathVariable @PositiveOrZero Long id) {
        return service.getById(id);
    }

    @GetMapping
    public List<Review> getAll(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return service.getAll(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable @PositiveOrZero Long id, @PathVariable @PositiveOrZero Long userId) {
        service.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable @PositiveOrZero Long id, @PathVariable @PositiveOrZero Long userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable @PositiveOrZero Long id, @PathVariable @PositiveOrZero Long userId) {
        service.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable @PositiveOrZero Long id, @PathVariable @PositiveOrZero Long userId) {
        service.removeDislike(id, userId);
    }
}
