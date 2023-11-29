package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.exceptions.IncorrectObjectModificationException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.DBFilmGenreStorage;
import ru.yandex.practicum.filmorate.storage.DBLikesStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final MpaStorage mpaStorage;
    private final DBFilmGenreStorage filmGenreStorage;
    private final DBLikesStorage likesStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(@Qualifier("dBFilmStorage") FilmStorage filmStorage,
                       @Qualifier("dBMpaStorage") MpaStorage mpaStorage,
                       DBFilmGenreStorage filmGenreStorage,
                       DBLikesStorage likesStorage,
                       @Qualifier("dBUserStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.mpaStorage = mpaStorage;
        this.filmGenreStorage = filmGenreStorage;
        this.likesStorage = likesStorage;
        this.userStorage = userStorage;
    }

    public List<Film> getAllFilms() {
        List<Film> films = new ArrayList<>(filmStorage.getAllFilms().values());
        Map<Integer, Mpa> mpa = mpaStorage.getAllMpa();

        for (Film film : films) {
            film.setMpa(mpa.get(film.getMpa().getId()));
            film.setGenres(filmGenreStorage.getFilmGenre(film.getId()));
            film.setUserLikes(likesStorage.getLikes(film.getId()));
        }

        return films;
    }

    public Film getFilm(int filmId) {
        Film film = collectFilm(filmId);
        return film;
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

    public Film addFilm(Film film) {
        filmStorage.addFilm(film);

        for (Genre genre : film.getGenres()) {
            filmGenreStorage.addFilmGenre(film.getId(), genre.getId());
        }

        for (int likes : film.getUserLikes()) {
            likesStorage.addLike(likes, film.getId());
        }

        film.setMpa(mpaStorage.getMpa(film.getMpa().getId()));
        film.setGenres(filmGenreStorage.getFilmGenre(film.getId()));
        return film;
    }

    public Film updateFilm(Film film) {
        Film oldFilmVersion = collectFilm(film.getId());
        filmStorage.updateFilm(film);

        filmGenreStorage.deleteFilmGenres(oldFilmVersion.getId());
        for (Genre genre : film.getGenres()) {
            filmGenreStorage.addFilmGenre(film.getId(), genre.getId());
        }

        likesStorage.deleteFilmLikes(oldFilmVersion.getId());
        for (int likes : film.getUserLikes()) {
            likesStorage.addLike(likes, film.getId());
        }

        film.setMpa(mpaStorage.getMpa(film.getMpa().getId()));
        film.setGenres(filmGenreStorage.getFilmGenre(film.getId()));
        return film;
    }

    public Film addLikeToFilm(int filmId, int userId) {
        // проверка на существование пользователя
        userStorage.getUser(userId);

        Film film = collectFilm(filmId);

        if (film.getUserLikes().contains(userId)) {
            throw new IncorrectObjectModificationException("Невозможно поставить уже существующий "
                    + "лайк пользователя с id = " + userId + " для фильма с id = " + filmId);
        }

        likesStorage.addLike(userId, filmId);
        film.getUserLikes().add(userId);

        return film;
    }

    public Film deleteFilm(int filmId) {
        Film film = filmStorage.deleteFilm(filmId);
        film.setMpa(mpaStorage.getMpa(film.getMpa().getId()));
        film.setGenres(filmGenreStorage.getFilmGenre(filmId));
        film.setUserLikes(likesStorage.getLikes(filmId));

        filmGenreStorage.deleteFilmGenres(filmId);
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

        return film;
    }

    private Film collectFilm(int filmId) {
        Film film = filmStorage.getFilm(filmId);
        film.setMpa(mpaStorage.getMpa(film.getMpa().getId()));
        film.setGenres(filmGenreStorage.getFilmGenre(filmId));
        film.setUserLikes(likesStorage.getLikes(filmId));
        return film;
    }
}