package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.List;

@Service
public interface GenreStorage {

    public List<Genre> getAllGenres();

    public Genre getGenreById(Long id);

}
