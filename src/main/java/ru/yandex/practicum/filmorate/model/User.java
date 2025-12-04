package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    Long id;
    @Email(message = "Некорректный ввод почты")
    @NotBlank(message = "Почта не может быть пустой")
    String email;
    @NotBlank(message = "Логин не может быть пустой")
    String login;
    String name;
    @Past(message = "Дата рождения не может быть в прошлом")
    LocalDate birthday;
}
