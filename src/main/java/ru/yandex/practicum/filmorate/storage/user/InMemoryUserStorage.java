package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Long, User> users = new HashMap<>();

    @Override
    public User addUser(User user) {

        User newUser = new User();
        if (user.getName() == null || user.getName().isBlank()) {
            log.warn("Пользователю {} назначено имя {} при регистрации.", user.getName(), user.getLogin());
            newUser.setName(user.getLogin());
        }

        newUser.setId(nextIdGenerate());
        newUser.setEmail(user.getEmail());
        newUser.setLogin(user.getLogin());
        if (newUser.getName() == null) {
            newUser.setName(user.getName());
        }
        newUser.setBirthday(user.getBirthday());

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем {} и логином {} зарегистрирован.", user.getName(), user.getLogin());
        return newUser;
    }

    @Override
    public User updateUser(User user) {
        User oldUser = users.get(user.getId());
        if (oldUser == null) {
            log.warn("Пользователь с id {} не найден в базе.", user.getId());
            throw new ValidationException("Пользователя не найден");
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

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем {} и логином {} обновил свой профиль.", user.getName(), user.getLogin());
        return newUser;
    }

    @Override
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
