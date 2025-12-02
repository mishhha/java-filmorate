package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final HashMap<Long, Film> films = new HashMap<>();

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        if(film.getName() == null) {

        }
        Film newFilm = new Film();
        newFilm.setId(nextIdGenerate());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        films.put(newFilm.getId(), newFilm);

        return newFilm;
    }

    @PutMapping
    public Film updateFilm(Film film) {
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        films.put(oldFilm.getId(), oldFilm);

        return oldFilm;
    }

    @GetMapping
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }


    public long nextIdGenerate() {
        long nextId = films.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
