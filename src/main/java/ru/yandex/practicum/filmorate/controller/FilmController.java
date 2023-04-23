package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private Map<Integer, Film> films = new HashMap<>();
    private int lastId = 1;

    @PostMapping
    public ResponseEntity<Film> addFilm(@RequestBody Film film) {
        log.info("Получен запрос POST /films");

        if (films.containsKey(film.getId())) {
            log.info("Film с указанным id УЖЕ существует");
            return ResponseEntity.badRequest().body(film);
        }

        if (validation(film)) {
            film.setId(lastId++);
            films.put(film.getId(), descriptionExistenceCheck(film));
            log.info("Пользователь добавлен: " + film);
            return ResponseEntity.ok().body(film);
        }
        return ResponseEntity.badRequest().body(film);
    }

    @PutMapping()
    public ResponseEntity<Film> updateFilm(@RequestBody Film film) {
        log.info("Получен запрос PUT /films");

        if (!films.containsKey(film.getId())) {
            log.info("Film с указанным id НЕ существует");
            return new ResponseEntity<>(film, HttpStatus.NOT_FOUND);
        }

        if (validation(film)) {
            films.put(film.getId(), descriptionExistenceCheck(film));
            log.info("Пользователь обновлён: " + film);
            return ResponseEntity.ok().body(film);
        }
        return ResponseEntity.badRequest().body(film);
    }

    @GetMapping
    public ResponseEntity<List<Film>> getAllFilms() {
        log.info("Получен запрос GET /films");
        return ResponseEntity.ok().body(new ArrayList<>(films.values()));
    }

    private boolean validation(Film film) {
        try {
            if (film.getName() == null || film.getReleaseDate() == null) {
                throw new ValidationException("1 или больше параметров являются null: " + film);
            }

            if (film.getName().isBlank()) {
                throw new ValidationException("Название фильма отсутствует");
            }

            if (film.getDescription() != null && film.getDescription().length() > 200) {
                throw new ValidationException("Длинна описания больше 200, а именно:" + film.getDescription().length());
            }

            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза указана раньше 28.12.1895");
            }

            if (film.getDuration() <= 0) {
                throw new ValidationException("Продолжительность фильмы не положительна");
            }

        } catch (ValidationException exception) {
            log.info(exception.toString());
            return false;
        }
        return true;
    }

    private Film descriptionExistenceCheck(Film film) {
        if (film.getDescription() == null) {
            film.setDescription("");
        }
        return film;
    }
}
