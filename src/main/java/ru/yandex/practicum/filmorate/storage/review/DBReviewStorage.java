package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Component("dBReviewStorage")
public class DBReviewStorage implements ReviewStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBReviewStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Review> getReviews(int filmId, int count) {
        String sqlQuery = "SELECT r.*, COALESCE(SUM(rul.is_positive), 0) AS useful \n" +
                "FROM reviews AS r \n" +
                "LEFT JOIN review_user_likes AS rul ON r.review_id = rul.REVIEW_ID \n";
        List<Review> reviews;

        if (filmId == 0) {
            sqlQuery += "GROUP BY r.review_id \n" +
                    "ORDER BY useful DESC \n";
            reviews = jdbcTemplate.query(sqlQuery, DBReviewStorage::createReview);
        } else {
            sqlQuery += "WHERE r.film_id = ? \n" +
                    "GROUP BY r.review_id \n" +
                    "ORDER BY useful DESC \n" +
                    "LIMIT ?;";
            reviews = jdbcTemplate.query(sqlQuery, DBReviewStorage::createReview, filmId, count);
        }

        return reviews;
    }

    @Override
    public Review getReview(int reviewId) {
        return checkContainsReview(reviewId);
    }

    @Override
    public Review addReview(Review review) {
        checkNonContainsReview(review.getReviewId());
        checkAddDuplicateReview(review);

        String sqlQuery = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        review.setReviewId((int) Objects.requireNonNull(keyHolder.getKey()));

        return review;
    }

    @Override
    public Review updateReview(Review review) {
        checkContainsReview(review.getReviewId());

        String sqlQuery = "UPDATE reviews " +
                "SET content = ?, is_positive = ? " +
                "WHERE review_id = ?;";

        jdbcTemplate.update(sqlQuery,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());

        return getReview(review.getReviewId());
    }

    @Override
    public void deleteReview(int reviewId) {
        String sqlQuery = "DELETE FROM reviews WHERE review_id = ?";

        jdbcTemplate.update(sqlQuery, reviewId);
    }

    public static Review createReview(ResultSet resultSet, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(resultSet.getInt("review_id"));
        review.setContent(resultSet.getString("content"));
        review.setIsPositive(resultSet.getBoolean("is_positive"));
        review.setUserId(resultSet.getInt("user_id"));
        review.setFilmId(resultSet.getInt("film_id"));
        review.setUseful(resultSet.getInt("useful"));
        return review;
    }

    private void checkNonContainsReview(int reviewId) {
        String sqlQuery = "SELECT * FROM reviews WHERE review_id = ?;";
        List<Review> review = jdbcTemplate.query(sqlQuery, DBReviewStorage::createReview, reviewId);

        if (review.size() > 0) {
            throw new AddExistObjectException("Review с указанным id = " + reviewId + " уже существует");
        }
    }

    private Review checkContainsReview(int reviewId) {
        String sqlQuery = "SELECT r.*, COALESCE(SUM(rul.is_positive), 0) AS useful \n" +
                "FROM reviews AS r \n" +
                "LEFT JOIN review_user_likes AS rul ON r.review_id = rul.REVIEW_ID \n" +
                "WHERE r.review_id = ? \n" +
                "GROUP BY r.review_id;";

        List<Review> review = jdbcTemplate.query(sqlQuery, DBReviewStorage::createReview, reviewId);

        if (review.size() != 1) {
            throw new UpdateNonExistObjectException("Review с указанным id = " + reviewId + " не существует " +
                    "или имеется больше 1");
        }

        return review.get(0);
    }

    private void checkAddDuplicateReview(Review review) {
        String sqlQuery = "SELECT * FROM reviews WHERE user_id = ? AND film_id = ?;";
        List<Review> result = jdbcTemplate.query(sqlQuery, DBReviewStorage::createReview,
                review.getUserId(), review.getFilmId());

        if (result.size() != 0) {
            throw new AddExistObjectException("Review для указанного фильма и пользователя уже существует " +
                    "filmId = " + review.getFilmId() + ", userId = " + review.getUserId());
        }
    }
}
