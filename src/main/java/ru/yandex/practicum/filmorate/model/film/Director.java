package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Director {

    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}