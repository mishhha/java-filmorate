package ru.yandex.practicum.filmorate.model.review;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class Review {
    private Long id;
    @NotBlank(message = "Текст отзыва пустой")
    private String content;
    @NotBlank(message = "Реакция пользователя не может быть пустой")
    private Boolean isPositive;
    @NotBlank(message = "Ид пользователя не может быть пустым")
    private Long userId;
    @NotBlank(message = "Ид фильма не может быть пустым")
    private Long filmId;

    @JsonIgnore
    private Map<Long, Byte> reactions = new HashMap<>();

    public Integer getUseful() {
        if (reactions.isEmpty()) {
            return 0;
        }

        return reactions.values().stream().mapToInt(Byte::intValue).sum();
    }
}
