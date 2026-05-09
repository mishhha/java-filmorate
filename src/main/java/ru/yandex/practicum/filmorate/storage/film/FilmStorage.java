package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;

public interface FilmStorage {

    List<Film> findTopFilmsByGenresAndYear(Long count, Long genreId, Long year);

    List<Film> searchFilmsByTitleAndDirector(String query);

    List<Film> searchFilmByDirector(String director);

    List<Film> findFilmsByPopular();

    List<Film> searchFilmBySubstring(String nameFilm);

    void deleteFilmById(Long filmId);

    List<Film> getFilms();

    List<Film> getTopFilms(int count);

    Film getFilmById(Long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    boolean addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    List<Film> getCommonFilms(Long userId, Long friendId);

    List<Film> getDirectorFilms(Long directorId, String sortBy);
}