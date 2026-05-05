package ru.yandex.practicum.filmorate.model.film;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    @JsonIgnore
    private Set<Long> likes = new HashSet<>();


    private Set<Genre> genres = new HashSet<>();

    @JsonProperty("mpa")
    private RatingMpa rating;

    private Set<Director> directors = new HashSet<>();


    public void addLike(Long userId) {
        if (userId != null) {
            likes.add(userId);
        }
    }

    public void removeLike(Long userId) {
        if (userId != null) {
            likes.remove(userId);
        }
    }


    public int getLikesCount() {
        return likes.size();
    }


    public void addGenre(Genre genre) {
        if (genre != null) {
            genres.add(genre);
        }
    }

    public void removeGenre(Genre genre) {
        if (genre != null) {
            genres.remove(genre);
        }
    }
}