package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component("dBUserStorage")
public class DBUserStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBUserStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Map<Integer, User> getAllUsers() {
        String sqlQuery = "SELECT * FROM users;";
        List<User> result = jdbcTemplate.query(sqlQuery, DBUserStorage::createUser);
        Map<Integer, User> users = new HashMap<>();

        for (User user : result) {
            users.put(user.getId(), user);
        }

        return users;
    }

    @Override
    public User getUser(int userId) {
        return checkContainsUser(userId);
    }

    @Override
    public User addUser(User user) {
        checkNonContainsUser(user.getId());
        checkAddDuplicateUser(user);

        fillingOptionalParameters(user);
        String sqlQuery = "INSERT INTO users (email, login, name, birthday)" +
                "VALUES (?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"user_id"});
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, java.sql.Date.valueOf((user.getBirthday())));
            return stmt;
        }, keyHolder);

        user.setId((int) Objects.requireNonNull(keyHolder.getKey()));

        return user;
    }

    @Override
    public User updateUser(User user) {
        checkContainsUser(user.getId());

        fillingOptionalParameters(user);
        String sqlQuery = "UPDATE users " +
                "SET email = ?, login = ?, name = ?, birthday = ? " +
                "WHERE user_id = ?;";

        jdbcTemplate.update(sqlQuery,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        return user;
    }

    @Override
    public User deleteUser(int userId) {
        User user = checkContainsUser(userId);
        user.setFriends(new HashSet<>(jdbcTemplate.query("SELECT friend_id FROM friends WHERE user_id = ?;",
                (resultSet, rowNum) -> resultSet.getInt("friend_id"),
                userId)));

        String sqlQuery = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, userId);
        sqlQuery = "DELETE FROM feed WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);
        sqlQuery = "DELETE FROM likes WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);
        sqlQuery = "DELETE FROM review_user_likes WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);
        sqlQuery = "DELETE FROM reviews WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);
        sqlQuery = "DELETE FROM users WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);

        return user;
    }

    public static User createUser(ResultSet resultSet, int rowNum) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("user_id"));
        user.setEmail(resultSet.getString("email"));
        user.setLogin(resultSet.getString("login"));
        user.setName(resultSet.getString("name"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());
        return user;
    }

    private void fillingOptionalParameters(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
        }
    }

    private void checkNonContainsUser(int userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?;";
        List<User> user = jdbcTemplate.query(sqlQuery, DBUserStorage::createUser, userId);

        if (user.size() > 0) {
            throw new AddExistObjectException("User с указанным id = " + userId + " уже существует");
        }
    }

    private User checkContainsUser(int userId) {
        String sqlQuery = "SELECT * FROM users WHERE user_id = ?;";
        List<User> user = jdbcTemplate.query(sqlQuery, DBUserStorage::createUser, userId);

        if (user.size() != 1) {
            throw new UpdateNonExistObjectException("User с указанным id = " + userId + " не существует " +
                    "или имеется больще 1");
        }

        return user.get(0);
    }

    private void checkAddDuplicateUser(User user) {
        String sqlQuery = "SELECT * FROM users WHERE email = ? OR login = ?;";
        List<User> result = jdbcTemplate.query(sqlQuery, DBUserStorage::createUser, user.getEmail(), user.getLogin());

        if (result.size() != 0) {
            if (user.getEmail().equals(result.get(0).getEmail()) && user.getLogin().equals(result.get(0).getLogin())) {
                throw new AddExistObjectException("Этот email и login уже заняты email = " + user.getEmail() +
                        ", login = " + user.getLogin());
            }
            if (user.getEmail().equals(result.get(0).getEmail())) {
                throw new AddExistObjectException("Этот email уже занят email = " + user.getEmail());
            }
            throw new AddExistObjectException("Этот login уже занят login = " + user.getLogin());
        }
    }
}
