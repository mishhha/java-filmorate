package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Set;

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

        User currentUser = userStorage.getUserById(userId);

        Set<Long> currentLikes = currentUser.getLikesFilms();

        User similarUser = null;
        long maxCommon = 0;

        for (User other : userStorage.getUsers()) {

            if (other.getId().equals(currentUser.getId())) continue;

            Set<Long> otherLikes = other.getLikesFilms();

            long common = currentLikes.stream()
                    .filter(otherLikes::contains)
                    .count();

            if (common > maxCommon) {
                maxCommon = common;
                similarUser = other;
            }
        }

        if (similarUser == null) {
            return List.of();
        }

        return similarUser.getLikesFilms().stream()
                .filter(id -> !currentLikes.contains(id))
                .map(filmStorage::getFilmById)
                .toList();
    }
}
