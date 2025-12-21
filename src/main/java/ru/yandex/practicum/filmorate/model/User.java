package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
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
    @Past(message = "Дата рождения не может быть в прошлом")
    private LocalDate birthday;
    private Set<Long> friendList;
    private Set<Long> likesFilms;

    public void addFriend(long id) {
        friendList.add(id);
    }

}
