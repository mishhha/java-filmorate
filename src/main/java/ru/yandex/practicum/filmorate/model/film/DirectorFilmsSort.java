package ru.yandex.practicum.filmorate.model.film;

public enum DirectorFilmsSort {
    YEAR,
    LIKES;

    // Преобразует строку в элемент перечисления
    public static DirectorFilmsSort from(String directorFilmsSort) {
        switch (directorFilmsSort.toLowerCase()) {
            case "year":
                return YEAR;
            case "likes":
                return LIKES;
            default:
                return null;
        }
    }
}
