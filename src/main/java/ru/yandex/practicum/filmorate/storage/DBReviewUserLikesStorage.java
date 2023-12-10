package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component("dBReviewUserLikesStorage")
public class DBReviewUserLikesStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBReviewUserLikesStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean addLikeToReview(int reviewId, int userId) {
        String sqlQuery = "INSERT INTO review_user_likes (review_id, user_id, is_positive)" +
                "VALUES (?, ?, ?);";
        return jdbcTemplate.update(sqlQuery, reviewId, userId, 1) == 1;
    }

    public boolean addDislikeToReview(int reviewId, int userId) {
        String sqlQuery = "INSERT INTO review_user_likes (review_id, user_id, is_positive)" +
                "VALUES (?, ?, ?);";
        return jdbcTemplate.update(sqlQuery, reviewId, userId, -1) == 1;
    }

    public boolean deleteReactionFromReview(int reviewId, int userId) {
        String sqlQuery = "DELETE FROM review_user_likes WHERE review_id = ? AND user_id = ?;";
        return jdbcTemplate.update(sqlQuery, reviewId, userId) == 1;
    }

    public void deleteAllReactionsFromReview(int reviewId) {
        String sqlQuery = "DELETE FROM review_user_likes WHERE review_id = ?";
        jdbcTemplate.update(sqlQuery, reviewId);
    }
}
