package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;

@Service
@Slf4j
@Data
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserStorage userStorage;

    public User addUser(User user) {
        User newUser = new User();
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Пользователю {} назначено имя {} при регистрации.", user.getName(), user.getLogin());
            newUser.setName(user.getLogin());
        }

        newUser.setId(userStorage.nextIdGenerate());
        newUser.setEmail(user.getEmail());
        newUser.setLogin(user.getLogin());
        if (newUser.getName() == null) {
            newUser.setName(user.getName());
        }
        newUser.setBirthday(user.getBirthday());

        return userStorage.addUser(newUser);
    }

    public User updateUser(User user) {
        User oldUser = userStorage.getUserById(user.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id {} не найден в базе.", user.getId());
            throw new NotFoundException("Пользователь не найден");
        }
        User newUser = new User();

        newUser.setId(oldUser.getId());
        newUser.setEmail(user.getEmail());
        newUser.setLogin(user.getLogin());
        if (user.getName() == null) {
            newUser.setName(user.getLogin());
        } else {
            newUser.setName(user.getName());
        }
        newUser.setBirthday(user.getBirthday());

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

    public User deleteFriend(Long id, Long friendId) {
        return userStorage.deleteFriend(id, friendId);
    }

    public User addFriend(Long id, Long friendId) {
        return userStorage.addFriend(id, friendId);
    }

    public List<User> getCommonFriend(Long id, Long otherId) {
        return userStorage.getCommonFriends(id, otherId);
    }



}
