package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.Event;
import ru.yandex.practicum.filmorate.model.user.User;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Long, User> users = new HashMap<>();
    private final HashMap<Long, Event> eventFeed = new HashMap<>();

    @Override
    public User addUser(User user) {
        users.put(user.getId(), user);
        log.info("Пользователь с именем {} и логином {} зарегистрирован.", user.getName(), user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        log.info("Пользователь с именем {} и логином {} обновил свой профиль.", user.getName(), user.getLogin());
        return user;
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
    public void deleteFriend(Long idUser, Long idFriend) {
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
        users.get(idFriend);
    }

    @Override
    public User addFriend(Long idUser, Long idFriend) {
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

    @Override
    public void addEvent(Event event) {
        User user = users.get(event.getUserId());
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }

        event.setId(eventFeed.size() + 1L);
        event.setTimestamp(LocalDateTime.now());
        eventFeed.put(event.getId(), event);

        log.info("Зарегистрирована операция {} по событию {}", event.getOperation(), event.getEventType());
    }

    @Override
    public List<Event> getEventList(Long userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь не найден.");
        }

        return eventFeed.values().stream()
                .filter(event -> userId.equals(event.getUserId()))
                .toList();
    }

    public long nextIdGenerate() {
        long nextId = users.keySet().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);

        return ++nextId;
    }
}
