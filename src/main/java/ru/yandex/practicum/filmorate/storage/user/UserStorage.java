package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.model.user.Event;

import java.util.List;

@Service
public interface UserStorage {

    User addUser(User user);

    User updateUser(User user);

    List<User> getUsers();

    User getUserById(Long id);

    List<User> getFriends(Long id);

    void deleteFriend(Long idUser, Long idFriend);

    User addFriend(Long idUser, Long idFriend);

    List<User> getCommonFriends(Long id, Long otherId);

    void addEvent (Event event);

    List<Event> getEventList(Long userId);
}
