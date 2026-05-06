package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Comparator;
import java.util.List;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@Data
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(UserStorage userStorage,
                       FilmStorage filmStorage) {
        this.userStorage = userStorage;
        this.filmStorage = filmStorage;
    }

    public void deleteUserById(Long userId) {
        userStorage.deleteUserById(userId);
    }

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Пользователю {} назначено имя {} при регистрации.", user.getName(), user.getLogin());
        }

        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        userStorage.getUserById(user.getId());
        return userStorage.updateUser(user);
    }

    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public User getUsersById(Long id) {
        return userStorage.getUserById(id);
    }

    public List<User> getFriends(Long id) {
        return userStorage.getFriends(id);
    }

    public void deleteFriend(Long id, Long friendId) {
        userStorage.deleteFriend(id, friendId);
    }

    public User addFriend(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public List<User> getCommonFriend(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public List<Film> getRecommendations(Long userId) {

        User user = userStorage.getUserById(userId);

        Set<Long> currentLikes = filmStorage.getFilms().stream()
                .filter(f -> f.getLikesSet().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Long similarUserId = userStorage.getUsers().stream()
                .filter(u -> !u.getId().equals(userId))
                .max(Comparator.comparingLong(other ->
                        filmStorage.getFilms().stream()
                                .filter(f -> f.getLikesSet().contains(other.getId()))
                                .map(Film::getId)
                                .filter(currentLikes::contains)
                                .count()
                ))
                .map(User::getId)
                .orElse(null);

        if (similarUserId == null) {
            return List.of();
        }

        Set<Long> similarLikes = filmStorage.getFilms().stream()
                .filter(f -> f.getLikesSet().contains(similarUserId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        return similarLikes.stream()
                .filter(id -> !currentLikes.contains(id))
                .map(filmStorage::getFilmById)
                .toList();
    }
}