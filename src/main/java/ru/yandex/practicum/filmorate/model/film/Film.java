package ru.yandex.practicum.filmorate.model.film;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;


@Data
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    private int likesCount;

    private Set<Genre> genres = new HashSet<>();

    @JsonProperty("mpa")
    private RatingMpa rating;

    private Set<Director> directors = new HashSet<>();
}