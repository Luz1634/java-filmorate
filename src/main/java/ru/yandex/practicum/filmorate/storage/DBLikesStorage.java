package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DBLikesStorage {

    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT_LIKES = "INSERT INTO likes (user_id, film_id) " +
            "VALUES (?, ?);";

    @Autowired
    public DBLikesStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Integer> getLikes(int filmId) {
        String sqlQuery = "SELECT user_id FROM likes WHERE film_id = ?;";
        return new HashSet<>(jdbcTemplate.query(sqlQuery,
                (resultSet, rowNum) -> resultSet.getInt("user_id"),
                filmId));
    }

    public Map<Integer, Set<Integer>> getLikes(List<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = String.format("SELECT user_id, film_id FROM likes WHERE film_id in (%s);", inSql);

        Map<Integer, Set<Integer>> result = films.stream().collect(Collectors.toMap(Film::getId, Film::getUserLikes));

        jdbcTemplate.query(sqlQuery, result.keySet().toArray(), (ResultSet rs) -> {
            int filmId = rs.getInt("film_id");
            int userId = rs.getInt("user_id");
            result.get(filmId).add(userId);
        });
        return result;
    }

    public void addLike(int userId, int filmId) {
        jdbcTemplate.update(SQL_INSERT_LIKES, userId, filmId);
    }

    public void addLike(int filmId, Set<Integer> likeUsers) {
        jdbcTemplate.batchUpdate(SQL_INSERT_LIKES,
                likeUsers,
                100,
                (PreparedStatement ps, Integer userId) -> {
                    ps.setInt(1, userId);
                    ps.setInt(2, filmId);
                });
    }

    public void deleteLike(int userId, int filmId) {
        String sqlQuery = "DELETE FROM likes WHERE user_id = ? AND film_id = ?;";
        jdbcTemplate.update(sqlQuery, userId, filmId);
    }

    public void deleteUserLikes(int userId) {
        String sqlQuery = "DELETE FROM likes WHERE user_id = ?;";
        jdbcTemplate.update(sqlQuery, userId);
    }

    public void deleteFilmLikes(int filmId) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    public Set<Integer> getLikesFilm(int userId) {
        String sqlQuery = "SELECT film_id FROM likes WHERE user_id = ?;";
        return new HashSet<>(jdbcTemplate.query(sqlQuery,
                (resultSet, rowNum) -> resultSet.getInt("film_id"),
                userId));
    }

    public Map<Integer, List<Integer>> getUsersLikes() {
        String sqlQuery = "select user_id, film_id from likes;";
        Map<Integer, List<Integer>> result = new HashMap<>();
        jdbcTemplate.query(sqlQuery, (ResultSet rs) -> {
            int thisUserId = rs.getInt("user_id");
            int thisFilmId = rs.getInt("film_id");
            if (!result.containsKey(thisUserId)) {
                result.put(thisUserId, new ArrayList<>());
            }
            result.get(thisUserId).add(thisFilmId);
        });
        return result;
    }
}
