INSERT INTO genres (id, name) VALUES
    (1, 'Комедия'),
    (2, 'Драма'),
    (3, 'Мультфильм'),
    (4, 'Триллер'),
    (5, 'Документальный'),
    (6, 'Боевик');

INSERT INTO mpa_ratings (id, name) VALUES
    (1, 'G'),
    (2, 'PG'),
    (3, 'PG-13'),
    (4, 'R'),
    (5, 'NC-17');


INSERT INTO friendship_statuses (id, name) VALUES
    (1, 'CONFIRMED'),
    (2, 'UNCONFIRMED');

-- Пользователь с id=1
INSERT INTO users (id, name, login, email, birthday)
VALUES (1, 'Test User', 'testuser', 'test@mail.ru', '1990-01-01');

-- Фильм с id=3
INSERT INTO films (id, name, description, release_date, duration, likes_count, mpa_rating_id)
VALUES (3, 'New film with director', 'Film with director', '1999-04-30', 120, 0, 3);