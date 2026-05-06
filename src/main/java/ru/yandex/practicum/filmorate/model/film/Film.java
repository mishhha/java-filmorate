package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
public class Film {

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;

    @JsonProperty("mpa")
    private RatingMpa rating;

    private Set<Genre> genres = new HashSet<>();
    private Set<Director> directors = new HashSet<>();
    @Getter(AccessLevel.NONE)
    private Set<Long> likes = new HashSet<>();

    public int getLikes() {
        return likes.size();
    }


    @JsonProperty("likesCount")
    public int getLikesCount() {
        return likes.size();
    }


    public Set<Long> getLikesSet() {
        return likes;
    }
}