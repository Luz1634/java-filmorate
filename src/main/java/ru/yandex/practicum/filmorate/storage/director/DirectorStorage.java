package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorStorage {
    List<Director> getAllDirectors();

    Director getDirector(int directorId);

    Map<Integer, Set<Director>> getDirectorByFilm(List<Film> films);

    Director addDirector(Director director);

    Director updateDirector(Director director);

    Director deleteDirector(int directorId);

    Director checkContainsDirector(int directorId);
}

