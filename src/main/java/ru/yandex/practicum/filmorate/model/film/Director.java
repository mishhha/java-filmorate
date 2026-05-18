package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {

    private Long id;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

}
