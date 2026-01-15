package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    InMemoryUserStorage userStorage;

    public UserService(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long idFriend, User initiator) {

        if (initiator.getFriendList().contains(idFriend)) {
            throw new ValidationException("Такой пользователь уже есть в друзьях");
        }

        Optional<User> userCheck = userStorage.getUsers().stream()
            .filter(user -> user.getId() == idFriend)
            .findFirst();

        if (userCheck.isPresent()) {
            User fromStorage = userCheck.get();
            initiator.addFriend(fromStorage.getId());
            fromStorage.addFriend(initiator.getId());
        }

    }

    public void removeFriend(long idFriend, User initiator) {

        if (!initiator.getFriendList().contains(idFriend)) {
            throw new ValidationException("Такого пользователя нет в друзьях");
        }

        initiator.getFriendList().remove(idFriend);

        Optional<User> userCheck = userStorage.getUsers().stream()
            .filter(user -> user.getId() == idFriend)
            .findFirst();

        if (userCheck.isPresent()) {
            User fromStorage = userCheck.get();
            fromStorage.getFriendList().remove(initiator.getId());
        }

    }

    public List<User> getMutualFriends(long idFriend, User initiator) {

        Set<Long> listInitiator = initiator.getFriendList();

        Optional<User> getFriend = userStorage.getUsers().stream()
            .filter(user -> user.getId() == idFriend)
            .findFirst();

        if (getFriend.isEmpty()) {
            return List.of();
        }
        User friend = getFriend.get();
        Set<Long> listFriend = friend.getFriendList();

        Set<Long> checkInitiator = listInitiator.stream()
            .filter(Long -> listFriend.contains(Long))
            .collect(Collectors.toSet());

        return userStorage.getUsers().stream()
            .filter(user -> checkInitiator.contains(user.getId()))
            .toList();

    }






}
