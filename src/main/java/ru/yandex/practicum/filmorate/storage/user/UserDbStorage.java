package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.exceptions.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Primary
@Repository
@Component("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper rowMapper;

    private static final String FIND_ALL = """
        SELECT * FROM users
    """;

    private static final String FIND_BY_ID = """
        SELECT * FROM users WHERE id = ?
    """;

    private static final String INSERT_USER_QUERY = """
        INSERT INTO users (name, email, login, birthday) VALUES (?, ?, ?, ?)
    """;

    private static final String INSERT_FRIENDS_USER_QUERY = """
        INSERT INTO friends (user_id, friend_id) VALUES (?, ?)
    """;

    private static final String UPDATE_USER_QUERY = """
        UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE id = ?
    """;

    private static final String DELETE_FRIEND_BY_ID_QUERY = """
        DELETE FROM friends WHERE user_id = ? AND friend_id = ?
    """;

    private static final String FIND_FRIENDS_QUERY = """
        SELECT u.*
        FROM users AS u
        JOIN friends AS f ON u.id = f.friend_id
        WHERE f.user_id = ?
    """;

    private static final String FIND_COMMON_FRIENDS = """
        SELECT u.*
        FROM users AS u
        JOIN friends AS f ON u.id = f.friend_id
        WHERE f.user_id IN (?, ?)
        GROUP BY u.id
        HAVING COUNT(*) > 1
    """;

    @Override
    public User updateUser(User user) {

        getUserById(user.getId());

        jdbc.update(UPDATE_USER_QUERY,
            user.getName(),
            user.getLogin(),
            user.getEmail(),
            user.getBirthday(),
            user.getId()
        );

        return getUserById(user.getId());
    }

    @Override
    public User addUser(User user) {

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                INSERT_USER_QUERY, Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());

        return getUserById(user.getId());
    }

    @Override
    public List<User> getUsers() {
        return jdbc.query(FIND_ALL, rowMapper);
    }

    @Override
    public User getUserById(Long id) {
        try {
            return jdbc.queryForObject(FIND_BY_ID, rowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
    }

    @Override
    public List<User> getFriends(Long id) {
        getUserById(id);
        return jdbc.query(FIND_FRIENDS_QUERY, rowMapper, id);
    }

    @Override
    public void deleteFriend(Long idUser, Long idFriend) {

        getUserById(idUser);
        getUserById(idFriend);

        int rowsDeleted = jdbc.update(DELETE_FRIEND_BY_ID_QUERY, idUser, idFriend);

        if (rowsDeleted == 0) {
            throw new NotFoundException("Дружба между пользователями " + idUser + " и " + idFriend + " не найдена");
        }
    }

    @Override
    public User addFriend(Long idUser, Long idFriend) {
        getUserById(idUser);
        getUserById(idFriend);
        int status1 = jdbc.update(INSERT_FRIENDS_USER_QUERY, idUser, idFriend);
        if (status1 > 0) {
            return getUserById(idFriend);
        }
        throw new ValidationException("Не удалось добавить дружбу между пользователями " + idUser + " и " + idFriend);
    }

    @Override
    public List<User> getCommonFriends(Long id, Long otherId) {
        return jdbc.query(FIND_COMMON_FRIENDS, rowMapper, id, otherId);
    }

}
