package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private static final LocalDate MIN_DATE_RELEASE = LocalDate.of(1895, 12, 28);
    private final HashMap<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> getFilms() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Film addFilm(Film film) {
        if (film.getDescription().length() > 200) {
            log.warn(
                "Превышена длина описания {} при создании.",
                film.getDescription().length()
            );
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(MIN_DATE_RELEASE)) {
            log.warn(
                "Указана неверная дата релиза, при создании, дата раньше допустимого значения {}",
                film.getReleaseDate()
            );
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }

        Film newFilm = new Film();
        newFilm.setId(nextIdGenerate());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм с названием {} создан.", film.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film film) {

        Film oldFilm = films.get(film.getId());
        if (oldFilm == null) {
            log.warn("Фильм с id {} не найден", film.getId());
            throw new ValidationException("Такой фильм не найден");
        }
        Film newFilm = new Film();

        newFilm.setId(oldFilm.getId());
        newFilm.setName(film.getName());
        newFilm.setDescription(film.getDescription());
        newFilm.setReleaseDate(film.getReleaseDate());
        newFilm.setDuration(film.getDuration());

        films.put(newFilm.getId(), newFilm);
        log.info("Фильм {} обновлен.", film.getName());
        return newFilm;
    }

    public long nextIdGenerate() {
        long nextId = films.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
