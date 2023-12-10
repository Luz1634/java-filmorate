package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Получен GET запрос на получение всех пользователей");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable("id") int userId) {
        log.info("Получен GET запрос на получение пользователя");
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable("id") int userId) {
        log.info("Получен GET запрос на получение друзей пользователя");
        return ResponseEntity.ok(userService.getFriends(userId));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getSameFriends(@PathVariable("id") int userId, @PathVariable int otherId) {
        log.info("Получен GET запрос на получение общего списока друзей");
        return ResponseEntity.ok(userService.getSameFriends(userId, otherId));
    }

    @PostMapping
    public ResponseEntity<User> addUser(@Valid @RequestBody User user) {
        log.info("Получен POST запрос на добавление нового пользователя");
        return ResponseEntity.ok(userService.addUser(user));
    }

    @PutMapping()
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        log.info("Получен PUT запрос на обновление пользователя");
        return ResponseEntity.ok(userService.updateUser(user));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> addFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен PUT запрос на добавление в друзья");
        return ResponseEntity.ok(userService.addFriend(userId, friendId));
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<User> deleteFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Получен DELETE запрос на удаление из друзей");
        return ResponseEntity.ok(userService.deleteFriend(userId, friendId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<User> deleteUser(@PathVariable("id") int id) {
        log.info("Запрос на удаление пользователя с id = {}", id);
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @GetMapping("/{id}/recommendations")
    public ResponseEntity<List<Film>> getRecommendations(@PathVariable("id") int userId) {
        log.info("Получен GET запрос на получение рекомендации по фильмам для просмотра");
        return ResponseEntity.ok(userService.getRecommendations(userId));
    }

    @GetMapping("/{id}/feed")
    public ResponseEntity<List<Event>> getFeed(@PathVariable("id") int userID) {
        log.info("Получен GET запрос на получение ленты событий");
        return ResponseEntity.ok(userService.getAllFeedByUserId(userID));
    }
}