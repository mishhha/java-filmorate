package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.Event;
import ru.yandex.practicum.filmorate.model.user.EventOperations;
import ru.yandex.practicum.filmorate.model.user.EventTypes;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;
import java.util.Set;

@Service
@Slf4j
@Data
public class UserService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       @Qualifier("filmDbStorage") FilmStorage filmStorage) {
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

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(id)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.REMOVE)
                .entityId(friendId)
                .build());
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.addFriend(id, friendId);

        //Добавление события в историю
        userStorage.addEvent(Event.builder()
                .userId(id)
                .eventType(EventTypes.FRIEND)
                .operation(EventOperations.ADD)
                .entityId(friendId)
                .build());

        return user;
    }

    public List<User> getCommonFriend(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }

    public List<Film> getRecommendations(Long userId) {
        User currentUser = userStorage.getUserById(userId);
        Set<Long> currentUserLikes = currentUser.getLikesFilms();

        User similarUser = null;
        long maxTotalLikes = 0;

        //Поиск пользователей с максимальным количеством пересечения по лайкам.
        for (User otherUser : userStorage.getUsers()) {
            //Если текущий пользователь, пропускаем итерацию
            if (otherUser.getId().equals(currentUser.getId())) {
                continue;
            }

            //Список лайков другого пользователя
            Set<Long> otherUserLikes = otherUser.getLikesFilms();

            //Подсчет общих лайков
            long totalLikes = currentUserLikes.stream()
                    .filter(otherUserLikes::contains)
                    .count();

            if (totalLikes > maxTotalLikes) {
                maxTotalLikes = totalLikes;
                similarUser = otherUser;
            }

        }

        // Если нет схожего по интересам пользователя
        if (similarUser == null) {
            return List.of();
        }

        //Идентификаторы рекомендованных фильмов
        List<Long> filmIds = similarUser.getLikesFilms().stream()
                .filter(filmId -> !currentUserLikes.contains(filmId))
                .toList();

        return filmIds.stream()
                .map(filmStorage::getFilmById)
                .toList();
    }

    public List<Event> getEventList(Long userId) {
        return userStorage.getEventList(userId);
    }
}
