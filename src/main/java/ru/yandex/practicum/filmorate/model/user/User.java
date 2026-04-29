package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class User {
    private Long id;
    @Email(message = "Некорректный ввод почты")
    @NotBlank(message = "Почта не может быть пустой")
    private String email;
    @NotBlank(message = "Логин не может быть пустой")
    private String login;
    private String name;
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    private Set<Long> friendList = new HashSet<Long>();
    private Set<Long> likesFilms = new HashSet<Long>();
    private Map<Long, FriendShipStatus> friendShips = new HashMap<>();

    public void addFriend(Long id) {
        friendList.add(id);
    }

    public void deleteFromFriends(Long id) {
        friendList.remove(id);
    }

    public void addLikesFilms(Long id) {
        likesFilms.add(id);
    }

}
