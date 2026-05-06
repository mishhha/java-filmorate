package ru.yandex.practicum.filmorate.model.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ToString
@EqualsAndHashCode(of = "id")
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

    // ✔ ЕДИНСТВЕННЫЙ источник дружбы
    private Map<Long, FriendShipStatus> friendShips = new HashMap<>();

    // -------------------------
    // УДАЛЕНО:
    // Set<Long> friendList
    // Set<Long> likesFilms
    // -------------------------

    // ✔ удобные методы (опционально)
    public void addFriend(Long id, FriendShipStatus status) {
        friendShips.put(id, status);
    }

    public void removeFriend(Long id) {
        friendShips.remove(id);
    }
}
