package ru.yandex.practicum.filmorate.model.film;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Director {

    @EqualsAndHashCode.Include
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}