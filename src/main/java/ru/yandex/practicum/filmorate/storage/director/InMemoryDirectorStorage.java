package ru.yandex.practicum.filmorate.storage.director;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.film.Director;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Repository("inMemoryDirectorStorage")
public class InMemoryDirectorStorage implements DirectorStorage {

    private final HashMap<Long, Director> directors = new HashMap<>();

    @Override
    public List<Director> getDirectors() {
        return new ArrayList<>(directors.values());
    }

    @Override
    public Director getDirectorById(Long id) {
        return directors.get(id);
    }

    @Override
    public Director addDirector(Director director) {
        director.setId(nextIdGenerate());
        directors.put(director.getId(), director);
        log.info("Режиссёр с id {} зарегистрирован.", director.getId());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        directors.put(director.getId(), director);
        log.info("Данные о режиссёре с id {} обновлены.", director.getId());
        return director;
    }

    @Override
    public void deleteDirectorById(Long directorId) {
        directors.remove(directorId);
    }

    @Override
    public void checkDirectorExists(Long id) {
        if (!directors.containsKey(id)) {
            log.warn("Режиссёр с id {} не найден", id);
            throw new NotFoundException("Режиссёр с id " + id + " не найден");
        }
    }

    private long nextIdGenerate() {
        long nextId = directors.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        return ++nextId;
    }
}
