package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.review.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryReviewStorage implements ReviewStorage {

    private Map<Long, Review> reviews = new HashMap<>();
    private long id = 1;

    private Map<Long, Map<Long, Integer>> reviewLikes = new HashMap<>();

    @Override
    public Review create(Review review) {
        review.setReviewId(id++);
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public Review update(Review review) {
        reviews.put(review.getReviewId(), review);
        return review;
    }

    @Override
    public void delete(Long id) {
        reviews.remove(id);
        reviewLikes.remove(id);
    }

    @Override
    public Review getById(Long id) {
        return reviews.get(id);
    }

    @Override
    public List<Review> getAll() {
        return new ArrayList<>(reviews.values());
    }
}