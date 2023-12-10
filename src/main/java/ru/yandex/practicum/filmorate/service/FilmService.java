package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.enumerations.EventType;
import ru.yandex.practicum.filmorate.model.enumerations.Operation;
import ru.yandex.practicum.filmorate.model.enumerations.SortType;
import ru.yandex.practicum.filmorate.storage.DBFilmDirectorStorage;
import ru.yandex.practicum.filmorate.storage.DBFilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.DBLikesStorage;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final DirectorStorage directorStorage;
    private final DBFilmGenreStorage filmGenreStorage;
    private final DBFilmDirectorStorage filmDirectorStorage;
    private final DBLikesStorage likesStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    @Autowired
    public FilmService(FilmStorage filmStorage,
                       DirectorStorage directorStorage,
                       DBFilmGenreStorage filmGenreStorage,
                       DBFilmDirectorStorage filmDirectorStorage,
                       DBLikesStorage likesStorage,
                       UserStorage userStorage,
                       EventService eventService) {
        this.filmStorage = filmStorage;
        this.directorStorage = directorStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.filmDirectorStorage = filmDirectorStorage;
        this.likesStorage = likesStorage;
        this.userStorage = userStorage;
        this.eventService = eventService;
    }

    public List<Film> getAllFilms() {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms().values());

        Map<Integer, Set<Genre>> filmGenresMap = filmGenreStorage.getFilmGenre(films);
        Map<Integer, Set<Integer>> filmLikesMap = likesStorage.getLikes(films);
        Map<Integer, Set<Director>> filmDirectorsMap = directorStorage.getDirectorByFilm(films);

        return films.stream()
                .peek(film -> {
                    film.setGenres(filmGenresMap.get(film.getId()).stream()
                            .sorted(Comparator.comparingInt(Genre::getId))
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                    film.setUserLikes(filmLikesMap.get(film.getId()));
                    film.setDirectors(filmDirectorsMap.get(film.getId()));
                })
                .collect(Collectors.toList());
    }

    public Film getFilm(int filmId) {
        return collectFilm(filmId);
    }

    public List<Film> getMostLikedFilms(int count) {
        List<Film> films = getAllFilms();
        List<Film> mostLikedFilms = new ArrayList<>(count);

        if (count > films.size()) {
            count = films.size();
        }

        for (int i = 0; i < count; ++i) {
            mostLikedFilms.add(films.get(i));
        }

        mostLikedFilms.sort((o1, o2) -> o2.getUserLikes().size() - o1.getUserLikes().size());

        for (int i = count; i < films.size(); ++i) {
            if (mostLikedFilms.get(count - 1).getUserLikes().size() >= films.get(i).getUserLikes().size()) {
                continue;
            }

            for (int j = 0; j < mostLikedFilms.size(); ++j) {
                if (films.get(i).getUserLikes().size() > mostLikedFilms.get(j).getUserLikes().size()) {
                    mostLikedFilms.remove(count - 1);
                    mostLikedFilms.add(j, films.get(i));
                    break;
                }
            }
        }

        return mostLikedFilms;
    }

    public List<Film> getDirectorFilms(int directorId, SortType sortBy) {
        directorStorage.checkContainsDirector(directorId);
        List<Film> films = filmDirectorStorage.getDirectorFilms(directorId, sortBy);

        Map<Integer, Set<Genre>> filmGenresMap = filmGenreStorage.getFilmGenre(films);
        Map<Integer, Set<Integer>> filmLikesMap = likesStorage.getLikes(films);
        Map<Integer, Set<Director>> filmDirectorsMap = directorStorage.getDirectorByFilm(films);

        return films.stream()
                .peek(film -> {
                    film.setGenres(filmGenresMap.get(film.getId()).stream()
                            .sorted(Comparator.comparingInt(Genre::getId))
                            .collect(Collectors.toCollection(LinkedHashSet::new)));
                    film.setDirectors(filmDirectorsMap.get(film.getId()));
                    film.setUserLikes(filmLikesMap.get(film.getId()));
                })
                .collect(Collectors.toList());
    }

    public Film addFilm(Film film) {
        filmStorage.addFilm(film);

        filmGenreStorage.addFilmGenre(film.getId(), film.getGenres());
        filmDirectorStorage.addFilmDirector(film.getId(), film.getDirectors());
        likesStorage.addLike(film.getId(), film.getUserLikes());

        film.setGenres(filmGenreStorage.getFilmGenre(film.getId()));
        film.setDirectors(filmDirectorStorage.getFilmDirector(film.getId()));

        return film;
    }

    public Film updateFilm(Film film) {
        Film oldFilmVersion = collectFilm(film.getId());
        filmStorage.updateFilm(film);

        filmGenreStorage.deleteFilmGenres(oldFilmVersion.getId());
        filmGenreStorage.addFilmGenre(film.getId(), film.getGenres());

        filmDirectorStorage.deleteFilmDirectors(oldFilmVersion.getId());
        filmDirectorStorage.addFilmDirector(film.getId(), film.getDirectors());

        likesStorage.deleteFilmLikes(oldFilmVersion.getId());
        likesStorage.addLike(film.getId(), film.getUserLikes());

        film.setGenres(filmGenreStorage.getFilmGenre(film.getId()));
        film.setDirectors(filmDirectorStorage.getFilmDirector(film.getId()));

        return film;
    }

    public Film addLikeToFilm(int filmId, int userId) {
        // проверка на существование пользователя
        userStorage.getUser(userId);

        Film film = collectFilm(filmId);

        if (!film.getUserLikes().contains(userId)) {
            likesStorage.addLike(userId, filmId);
        }
        film.getUserLikes().add(userId);

        eventService.crete(userId, filmId, EventType.LIKE, Operation.ADD);

        return film;
    }

    public Film deleteFilm(int filmId) {
        Film film = filmStorage.deleteFilm(filmId);
        film.setGenres(filmGenreStorage.getFilmGenre(filmId));
        film.setDirectors(filmDirectorStorage.getFilmDirector(filmId));
        film.setUserLikes(likesStorage.getLikes(filmId));

        filmGenreStorage.deleteFilmGenres(filmId);
        filmDirectorStorage.deleteFilmDirectors(filmId);
        likesStorage.deleteFilmLikes(filmId);

        return film;
    }

    public Film deleteLikeToFilm(int filmId, int userId) {
        // проверка на существование пользователя
        userStorage.getUser(userId);

        Film film = collectFilm(filmId);

        if (!film.getUserLikes().contains(userId)) {
            throw new UpdateNonExistObjectException("Невозможно удалить несуществующий "
                    + "лайк пользователя с id = " + userId + " для фильма с id = " + filmId);
        }

        likesStorage.deleteLike(userId, filmId);
        film.getUserLikes().remove(userId);

        eventService.crete(userId, filmId, EventType.LIKE, Operation.REMOVE);

        return film;
    }

    public List<Film> getMostLikedFilmsByGenreAndYear(int count, int genreID, int year) {
        List<Film> filmList = filmStorage.getMostLikedFilmsByGenreAndYear(count, genreID, year);

        Map<Integer, Set<Genre>> filmGenresMap = filmGenreStorage.getFilmGenre(filmList);
        Map<Integer, Set<Integer>> filmLikesMap = likesStorage.getLikes(filmList);
        Map<Integer, Set<Director>> filmDirectorsMap = directorStorage.getDirectorByFilm(filmList);

        return filmList.stream().peek(film -> {
            film.setGenres(filmGenresMap.get(film.getId()).stream()
                    .sorted(Comparator.comparingInt(Genre::getId))
                    .collect(Collectors.toCollection(LinkedHashSet::new)));
            film.setUserLikes(filmLikesMap.get(film.getId()));
            film.setDirectors(filmDirectorsMap.get(film.getId()));
        }).collect(Collectors.toList());
    }

    private Film collectFilm(int filmId) {
        Film film = filmStorage.getFilm(filmId);
        film.setGenres(filmGenreStorage.getFilmGenre(filmId));
        film.setDirectors(filmDirectorStorage.getFilmDirector(filmId));
        film.setUserLikes(likesStorage.getLikes(filmId));
        return film;
    }

    public List<Film> getCommonFriendFilms(int userId, int friendId) {
        userStorage.getUser(userId);
        userStorage.getUser(friendId);

        List<Film> films = filmStorage.getCommonFriendFilms(userId, friendId);

        if (films.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Integer, Set<Genre>> filmGenresMap = filmGenreStorage.getFilmGenre(films);
        Map<Integer, Set<Integer>> filmLikesMap = likesStorage.getLikes(films);

        return films.stream()
                .peek(film -> {
                    film.setGenres(filmGenresMap.get(film.getId()));
                    film.setUserLikes(filmLikesMap.get(film.getId()));
                })
                .sorted((f1, f2) -> f2.getUserLikes().size() - f1.getUserLikes().size())
                .collect(Collectors.toList());
    }

    public List<Film> searchFilms(String query, String by) {
        boolean isDirector = by.contains("director");
        boolean isTitle = by.contains("title");
        return filmStorage.searchFilm(query, isDirector, isTitle).stream()
                .peek(film -> film.setGenres(filmGenreStorage.getFilmGenre(film.getId())))
                .peek(film -> film.setDirectors(filmDirectorStorage.getFilmDirector(film.getId())))
                .collect(Collectors.toList());
    }
}