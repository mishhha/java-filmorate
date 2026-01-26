package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User addUser(@RequestBody @Valid User user) {
        return userService.addUser(user);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody @Valid User user) {
        return userService.updateUser(user);
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping("/{id}")
    public User getUsersById(@PathVariable Long id) {
        return userService.getUsersById(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriendsById(@PathVariable Long id) {
        if (id <= 0) {
            throw new ValidationException(
                "ID пользователя должен быть положительный " +
                    "<" + id + ">."
                );
        }
        return userService.getFriends(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFriend(@PathVariable Long id, @PathVariable Long friendId) {
        if (id <= 0 || friendId <= 0) {
            throw new ValidationException(
                "ID пользователя и ID друга должен быть положительными " +
                    "<" + id + ">" + "<" + friendId + ">"
            );
        }
        return userService.deleteFriend(id, friendId);
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
        return userService.addFriend(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriend(@PathVariable Long id, @PathVariable Long otherId) {
        if (id <= 0 || otherId <= 0) {
            throw new ValidationException(
                "ID пользователя и ID друга должен быть положительными " +
                    "<" + id + ">" + "<" + otherId + ">"
            );
        }
        return userService.getCommonFriend(id, otherId);
    }

}