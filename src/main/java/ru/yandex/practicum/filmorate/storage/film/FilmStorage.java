package ru.yandex.practicum.filmorate.storage.film;


import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;


public interface FilmStorage {

     void deleteFilmById(Long filmId);

    List<Film> getFilms();

    List<Film> getPopularFilms(int count, Long genreId, Integer year);

    Film getFilmById(Long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

     void addLike(Long filmId, Long userId);

     void removeLike(Long filmId, Long userId);

     List<Film> getCommonFilms(Long userId, Long friendId);
}