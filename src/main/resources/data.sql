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


INSERT INTO friendship_statuses (name) VALUES
    ('CONFIRMED'),
    ('UNCONFIRMED');

/* INSERT INTO users (id, name, email, login, birthday) VALUES
    (1, 'Test User', 'test@mail.ru', 'test_login', '1990-01-01'),
    (2, 'Second User', 'second@mail.ru', 'second_login', '1992-03-03'),
    (3, 'Third User', 'third@mail.ru', 'third_login', '1995-05-05');

INSERT INTO films (id, name, description, release_date, duration, mpa_rating_id) VALUES
    (1, 'Test Film', 'Test Description', '2020-01-01', 120,1);

INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1);

ALTER TABLE users ALTER COLUMN id RESTART WITH 4;
ALTER TABLE films ALTER COLUMN id RESTART WITH 2;

 */