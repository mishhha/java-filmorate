package ru.yandex.practicum.filmorate.model.review;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Review {
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Map<Long, Byte> reactions = new HashMap<>();

    public Integer getUseful() {
        if (reactions.isEmpty()) {
            return 0;
        }

        return reactions.values().stream().mapToInt(Byte::intValue).sum();
    }
}
