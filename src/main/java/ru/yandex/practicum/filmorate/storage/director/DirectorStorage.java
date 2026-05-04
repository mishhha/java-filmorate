package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.List;

public interface DirectorStorage {

    public List<Director> getDirectors();

    public Director getDirectorById(Long id);

    public Director addDirector(Director director);

    public Director updateDirector(Director director);

    public void deleteDirectorById(Long directorId);

    public void checkDirectorExists(Long id);
}
