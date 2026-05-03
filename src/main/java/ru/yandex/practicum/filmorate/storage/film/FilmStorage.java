package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.util.List;

@Service
public interface FilmStorage {

    public List<Film> searchFilmByDirector(String director);

    public List<Film> findFilmsByPopular();

    public List<Film> searchFilmBySubstring(String nameFilm);

    public void deleteFilmById(Long filmId);

    List<Film> getFilms();

    List<Film> getTopFilms(int count);

    Film getFilmById(Long id);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    public void addLike(Long filmId, Long userId);

    public void removeLike(Long filmId, Long userId);

    public List<Film> getCommonFilms(Long userId, Long friendId);

    public List<Film> getDirectorFilms(Long directorId, String sortBy);
}