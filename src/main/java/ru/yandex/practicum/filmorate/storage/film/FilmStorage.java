package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;

@Service
public interface FilmStorage {

    List<Film> getFilms();

    List<Film> getTopFilms(int count);

    Film getFilmById(Long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    public void addLike(Long id, Long filmId);

    public void removeLike(Long id, Long filmId);

}