package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/directors")
@Slf4j
public class DirectorController {
    private final DirectorService directorService;

    @Autowired
    public DirectorController(DirectorService directorService) {
        this.directorService = directorService;
    }

    @GetMapping
    public ResponseEntity<List<Director>> getAllDirectors() {
        log.info("Получен GET запрос на получение всех директоров");
        return ResponseEntity.ok(directorService.getAllDirectors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Director> getDirector(@PathVariable("id") int directorId) {
        log.info("Получен GET запрос на получение директоров по id");
        return ResponseEntity.ok(directorService.getDirector(directorId));
    }

    @PostMapping
    public ResponseEntity<Director> addDirector(@Valid @RequestBody Director director) {
        log.info("Получен POST запрос на добавление нового директора");
        return ResponseEntity.ok(directorService.addDirector(director));
    }

    @PutMapping()
    public ResponseEntity<Director> updateDirector(@Valid @RequestBody Director director) {
        log.info("Получен PUT запрос на обновление директора");
        return ResponseEntity.ok(directorService.updateDirector(director));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Director> deleteDirector(@PathVariable("id") int directorId) {
        log.info("Получен DELETE запрос на удаление директора");
        return ResponseEntity.ok(directorService.deleteDirector(directorId));
    }
}
