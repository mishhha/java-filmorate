package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final HashMap<Long, User> users = new HashMap<>();

    @PostMapping
    public User addUser(@RequestBody User user) {
        if(user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Пользователь {} указал неверный email {} при регистрации.", user.getName(), user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }

        if(user.getLogin() == null || user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.warn("Пользователь {} указал неверный логин {} при регистрации.", user.getName(), user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }

        User newUser = new User();
        if(user.getName() == null || user.getName().isBlank() || user.getName().contains(" ")) {
            log.warn("Пользователю {} назначено имя {} при регистрации.", user.getName(), user.getLogin());
            newUser.setName(user.getLogin());
        }

        if(user.getBirthday() == null || user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Пользователь {} указал неверную дату рождения {} при регистрации.",
                        user.getName(), user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }

        newUser.setId(nextIdGenerate());
        newUser.setEmail(user.getEmail());
        newUser.setLogin(user.getLogin());
        if(newUser.getName() == null) {
            newUser.setName(user.getName());
        }
        newUser.setBirthday(user.getBirthday());

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем {} и логином {} зарегистрирован.", user.getName(), user.getLogin());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        User oldUser = users.get(user.getId());
        User newUser = new User();

        newUser.setId(oldUser.getId());

        if(user.getName() == null) {
            newUser.setName(oldUser.getName());
        } else {
            newUser.setName(user.getName());
        }

        if(user.getEmail() == null) {
            newUser.setEmail(oldUser.getEmail());
        } else if(user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Пользователь {} указал неверный email {} при редактировании.", user.getName(), user.getEmail());
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        } else {
            newUser.setEmail(user.getEmail());
        }

        if(user.getLogin() == null) {
            newUser.setLogin(oldUser.getLogin());
        } else if(user.getLogin().contains(" ") || user.getLogin().isBlank()) {
            log.warn("Пользователь {} указал неверный логин {} при редактировании.", user.getName(), user.getLogin());
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        } else {
            newUser.setLogin(user.getLogin());
        }

        if(user.getBirthday() == null) {
            newUser.setBirthday(oldUser.getBirthday());
        } else if(user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Пользователь {} указал неверную дату рождения {} при редактировании.",
                user.getName(), user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем.");
        } else {
            newUser.setBirthday(user.getBirthday());
        }

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем {} и логином {} обновил свой профиль.", user.getName(), user.getLogin());
        return newUser;
    }

    @GetMapping
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }


    public long nextIdGenerate() {
        long nextId = users.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
