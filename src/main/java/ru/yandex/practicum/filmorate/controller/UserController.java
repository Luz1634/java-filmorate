package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private Map<Integer, User> users = new HashMap<>();
    private int lastId = 1;

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) {
        log.info("Получен запрос POST /users");

        if (users.containsKey(user.getId())) {
            log.info("User с указанным id УЖЕ существует");
            return ResponseEntity.badRequest().body(user);
        }

        if (validation(user)) {
            user.setId(lastId++);
            users.put(user.getId(), nameExistenceCheck(user));
            log.info("Пользователь добавлен: " + user);
            return ResponseEntity.ok().body(user);
        }
        return ResponseEntity.badRequest().body(user);
    }

    @PutMapping()
    public ResponseEntity<User> updateUser(@RequestBody User user) {
        log.info("Получен запрос PUT /users");

        if (!users.containsKey(user.getId())) {
            log.info("User с указанным id НЕ существует");
            return new ResponseEntity<>(user, HttpStatus.NOT_FOUND);
        }

        if (validation(user)) {
            users.put(user.getId(), nameExistenceCheck(user));
            log.info("Пользователь обновлён: " + user);
            return ResponseEntity.ok().body(user);
        }
        return ResponseEntity.badRequest().body(user);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        log.info("Получен запрос GET /users");
        return ResponseEntity.ok().body(new ArrayList<>(users.values()));
    }

    private boolean validation(User user) {
        try {
            if (user.getEmail() == null || user.getLogin() == null || user.getBirthday() == null) {
                throw new ValidationException("1 или больше параметров являются null: " + user);
            }

            if (user.getEmail().isBlank() || !user.getEmail().contains("@")) {
                throw new ValidationException("Email написан неправильно: " + user.getEmail());
            }

            if (user.getLogin().isBlank() || user.getLogin().contains(" ")) {
                throw new ValidationException("Login написан неправильно: " + user.getLogin());
            }

            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения указана в будущем: " + user.getBirthday());
            }

        } catch (ValidationException exception) {
            log.info(exception.toString());
            return false;
        }
        return true;
    }

    private User nameExistenceCheck(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
