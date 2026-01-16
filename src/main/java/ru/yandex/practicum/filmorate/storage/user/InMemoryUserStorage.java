package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

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

        users.put(newUser.getId(), newUser);
        log.info("Пользователь с именем {} и логином {} обновил свой профиль.", user.getName(), user.getLogin());
        return newUser;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        return user;
    }

    @Override
    public List<User> getFriends(Long id) {

        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        Set<Long> getFriendId = user.getFriendList();

        return getFriendId.stream()
            .map(users::get)
            .filter(Objects::nonNull)
            .toList();
    }

    @Override
    public User deleteFriend(Long idUser, Long idFriend) {
        User user = users.get(idUser);
        if (user == null) {
            throw new NotFoundException("Пользователь c ID: " + idUser + " не найден.");
        }
        user.deleteFromFriends(idFriend);

        User userFriend = users.get(idFriend);
        if (userFriend == null) {
            throw new NotFoundException("Пользователь c ID: " + idFriend + " не найден.");
        }
        userFriend.deleteFromFriends(idUser);
        return users.get(idFriend);
    }

    @Override
    public User addFriendById(Long idUser, Long idFriend) {
        User user = users.get(idUser);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }
        user.addFriend(idFriend);
        User userFriend = users.get(idFriend);
        if (userFriend == null) {
            throw new NotFoundException("Пользователь друг не найден.");
        }
        userFriend.addFriend(idUser);
        return userFriend;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {

        Set<Long> otherFriendList = users.get(otherId).getFriendList();

        return users.get(id).getFriendList().stream()
            .filter(otherFriendList::contains)
            .map(users::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    }

    public long nextIdGenerate() {
        long nextId = users.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
