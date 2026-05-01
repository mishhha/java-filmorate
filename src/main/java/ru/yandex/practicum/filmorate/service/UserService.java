package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.user.Event;
import ru.yandex.practicum.filmorate.model.user.EventOperations;
import ru.yandex.practicum.filmorate.model.user.EventTypes;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
@Data
public class UserService {

    private final UserStorage userStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
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

    public List<Event> getEventList(Long userId) {
        return userStorage.getEventList(userId);
    }
}
