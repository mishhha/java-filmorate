-- ЖАНРЫ
INSERT INTO genres (name) VALUES
('Комедия'),
('Драма'),
('Мультфильм'),
('Триллер'),
('Документальный'),
('Боевик');

-- РЕЙТИНГИ
INSERT INTO mpa_ratings (name) VALUES
('G'),
('PG'),
('PG-13'),
('R'),
('NC-17');

-- СТАТУСЫ ДРУЖБЫ
INSERT INTO friendship_statuses (id, name) VALUES
(1, 'CONFIRMED'),
(2, 'UNCONFIRMED');

-- ПОЛЬЗОВАТЕЛИ (БЕЗ ID!)
INSERT INTO users (name, email, login, birthday) VALUES
('Test User', 'test@mail.ru', 'test_login', '1990-01-01'),
('Second User', 'second@mail.ru', 'second_login', '1992-03-03'),
('Third User', 'third@mail.ru', 'third_login', '1995-05-05');

-- ФИЛЬМЫ (БЕЗ ID + likes_count = 0)
INSERT INTO films (name, description, release_date, duration, mpa_rating_id, likes_count)
VALUES
('Test Film', 'Test Description', '2020-01-01', 120, 1, 0),
('Бриллиантовая рука', 'Test Description2', '1969-01-01', 94, 2, 0),
('Чебурашка', 'Test Description3', '2023-01-01', 113, 3, 0),
('Стражи Галактики', 'Test Description4', '2023-01-01', 121, 3, 0);

-- ЛАЙКИ
INSERT INTO likes (film_id, user_id)
VALUES
(2, 1),
(3, 1),
(4, 1),
(2, 2),
(3, 2),
(2, 3),
(1, 3);

-- СИНХРОНИЗАЦИЯ likes_count
UPDATE films f
SET likes_count = (
    SELECT COUNT(*)
    FROM likes l
    WHERE l.film_id = f.id
);

-- ЖАНРЫ ФИЛЬМОВ
INSERT INTO film_genres (film_id, genre_id)
VALUES
(1, 1),
(2, 2),
(3, 1),
(4, 6);

-- РЕЖИССЁРЫ
INSERT INTO directors (name) VALUES
('Стивен Спилберг'),
('Кристофер Нолан'),
('Квентин Тарантино');

-- СВЯЗЬ ФИЛЬМЫ-РЕЖИССЁРЫ
INSERT INTO films_directors (film_id, director_id)
VALUES
(1, 1),
(2, 2),
(3, 1),
(4, 3);