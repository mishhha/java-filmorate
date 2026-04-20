package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.RatingMpa;
import ru.yandex.practicum.filmorate.service.RatingMpaService;

import java.util.List;

@Data
@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class RatingMpaController {

    private final RatingMpaService ratingMpaService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<RatingMpa> getAllRatingsMpa() {
        return ratingMpaService.getAllRatingsMpa();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RatingMpa getGenreById(@PathVariable @PositiveOrZero Long id) {
        return ratingMpaService.getRatingMpaById(id);
    }

}
