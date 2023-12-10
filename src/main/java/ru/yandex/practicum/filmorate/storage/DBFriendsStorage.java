package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DBFriendsStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBFriendsStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Integer> getFriends(int userId) {
        String sqlQuery = "SELECT friend_id FROM friends WHERE user_id = ?;";
        return new HashSet<>(jdbcTemplate.query(sqlQuery,
                (resultSet, rowNum) -> resultSet.getInt("friend_id"),
                userId));
    }

    public void addFriend(int userId, int friendId) {
        String sqlQuery = "INSERT INTO friends (user_id, friend_id) " +
                "VALUES (?, ?);";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    public void deleteFriend(int userId, int friendId) {
        String sqlQuery = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, friendId);
    }

    public void deleteFriends(int userId) {
        String sqlQuery = "DELETE FROM friends WHERE user_id = ? OR friend_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, userId);
    }
}
