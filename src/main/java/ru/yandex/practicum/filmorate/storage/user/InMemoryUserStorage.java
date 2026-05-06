package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.model.user.FriendShipStatus;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public void deleteUserById(Long userId) {
        users.remove(userId);
    }

    @Override
    public User addUser(User user) {
        if (user.getId() == null) {
            user.setId(nextIdGenerate());
        }

        if (user.getFriendShips() == null) {
            user.setFriendShips(new HashMap<>());
        }

        users.put(user.getId(), user);

        log.info("Пользователь {} зарегистрирован", user.getLogin());
        return user;
    }

    @Override
    public User updateUser(User user) {

        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }

        User existing = users.get(user.getId());

        existing.setEmail(user.getEmail());
        existing.setLogin(user.getLogin());
        existing.setName(user.getName());
        existing.setBirthday(user.getBirthday());

        if (user.getFriendShips() != null) {
            existing.setFriendShips(user.getFriendShips());
        }

        return existing;
    }

    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }

    @Override

    public User getUserById(Long id) {
        User user = users.get(id);

        if (user == null) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        return user;
    }

    @Override
    public List<User> getFriends(Long id) {

        User user = getUserById(id);

        return user.getFriendShips().keySet().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void deleteFriend(Long idUser, Long idFriend) {

        User user = getUserById(idUser);
        User friend = getUserById(idFriend);

        user.getFriendShips().remove(idFriend);
        friend.getFriendShips().remove(idUser);
    }

    @Override
    public User addFriend(Long idUser, Long idFriend) {

        User user = getUserById(idUser);
        User friend = getUserById(idFriend);

        user.getFriendShips().put(idFriend, FriendShipStatus.UNCONFIRMED);
        friend.getFriendShips().put(idUser, FriendShipStatus.UNCONFIRMED);

        return user;
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {

        User user = getUserById(id);
        User other = getUserById(otherId);

        Set<Long> first = user.getFriendShips().keySet();
        Set<Long> second = other.getFriendShips().keySet();

        return first.stream()
                .filter(second::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public long nextIdGenerate() {
        return users.keySet().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}
