package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewStorage {
    public List<Review> getReviews(int filmId, int count);

    public Review getReview(int reviewId);

    public Review addReview(Review review);

    public Review updateReview(Review review);

    public void deleteReview(int reviewId);
}
