package ru.yandex.practicum.filmorate.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.util.List;

@Service
@Data
@RequiredArgsConstructor
public class UserService {

    private final InMemoryUserStorage userStorage;

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
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
