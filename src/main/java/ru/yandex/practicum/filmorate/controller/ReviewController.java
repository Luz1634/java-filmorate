package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<List<Review>> getReviews(@RequestParam(defaultValue = "0") int filmId,
                                                   @Positive @RequestParam(defaultValue = "10") int count) {
        log.info("Получен GET запрос на получение всех отзывов с filmId = {}, count = {}", filmId, count);
        return ResponseEntity.ok(reviewService.getReviews(filmId, count));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Review> getReview(@PathVariable("id") int reviewId) {
        log.info("Получен GET запрос на получение отзыва с reviewId = {}", reviewId);
        return ResponseEntity.ok(reviewService.getReview(reviewId));
    }

    @PostMapping
    public ResponseEntity<Review> addReview(@Valid @RequestBody Review review) {
        log.info("Получен POST запрос на добавление отзыва");
        return ResponseEntity.ok(reviewService.addReview(review));
    }

    @PutMapping
    public ResponseEntity<Review> updateReview(@Valid @RequestBody Review review) {
        log.info("Получен PUT запрос на обновление отзыва");
        return ResponseEntity.ok(reviewService.updateReview(review));
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> addLikeToReview(@PathVariable("id") int reviewId, @PathVariable int userId) {
        log.info("Получен PUT запрос на добавление лайка для отзыва");
        return ResponseEntity.ok(reviewService.addLikeToReview(reviewId, userId));
    }

    @PutMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> addDislikeToReview(@PathVariable("id") int reviewId, @PathVariable int userId) {
        log.info("Получен PUT запрос на добавление дизлайка для отзыва");
        return ResponseEntity.ok(reviewService.addDislikeToReview(reviewId, userId));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Review> deleteReview(@PathVariable("id") int reviewId) {
        log.info("Получен DELETE запрос на удаление отзыва");
        return ResponseEntity.ok(reviewService.deleteReview(reviewId));
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Review> deleteLikeToReview(@PathVariable("id") int reviewId, @PathVariable int userId) {
        log.info("Получен DELETE запрос на удаление лайка для отзыва");
        return ResponseEntity.ok(reviewService.deleteLikeToReview(reviewId, userId));
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public ResponseEntity<Review> deleteDislikeToReview(@PathVariable("id") int reviewId, @PathVariable int userId) {
        log.info("Получен DELETE запрос на удаление дизлайка для отзыва");
        return ResponseEntity.ok(reviewService.deleteDislikeToReview(reviewId, userId));
    }
}
