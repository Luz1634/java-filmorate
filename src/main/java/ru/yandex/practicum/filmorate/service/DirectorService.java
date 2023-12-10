package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DBFilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.List;

@Component
public class DirectorService {

    private final DirectorStorage directorStorage;
    private final DBFilmDirectorStorage filmDirectorStorage;

    @Autowired
    public DirectorService(DirectorStorage directorStorage, DBFilmDirectorStorage filmDirectorStorage) {
        this.directorStorage = directorStorage;
        this.filmDirectorStorage = filmDirectorStorage;
    }

    public List<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public Director getDirector(int directorId) {
        return directorStorage.getDirector(directorId);
    }

    public Director addDirector(Director director) {
        return directorStorage.addDirector(director);
    }

    public Director updateDirector(Director director) {
        return directorStorage.updateDirector(director);
    }

    public Director deleteDirector(int directorId) {
        return directorStorage.deleteDirector(directorId);
    }
}
