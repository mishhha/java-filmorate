package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    UserStorage userStorage;

    @Autowired
    public UserController(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid User user) {
        return userStorage.addUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody @Valid User user) {
        return userStorage.updateUser(user);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    @GetMapping("/{id}")
    public User getUsersById(@PathVariable Long id) {
        return userStorage.getUserById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsById(@PathVariable Long id) {
        if (id <= 0) {
            throw new ValidationException(
                "ID пользователя должен быть положительный " +
                    "<" + id + ">."
                );
        }
        return userStorage.getFriends(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (id <= 0 || friendId <= 0) {
            throw new ValidationException(
                "ID пользователя и ID друга должен быть положительными " +
                    "<" + id + ">" + "<" + friendId + ">"
            );
        }
        return userStorage.deleteFriend(id, friendId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (id <= 0 || friendId <= 0) {
            throw new ValidationException(
                "ID пользователя и ID друга должен быть положительными " +
                    "<" + id + ">" + "<" + friendId + ">"
            );
        }
        return userStorage.addFriendById(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Long id, @PathVariable Long otherId) {
        if (id <= 0 || otherId <= 0) {
            throw new ValidationException(
                "ID пользователя и ID друга должен быть положительными " +
                    "<" + id + ">" + "<" + otherId + ">"
            );
        }
        return userStorage.getCommonFriends(id, otherId);
    }

}