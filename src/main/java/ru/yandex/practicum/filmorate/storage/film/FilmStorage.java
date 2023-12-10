package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Map;

public interface FilmStorage {

    Map<Integer, Film> getAllFilms();

    Film getFilm(int filmId);

    List<Film> getFilm(List<Integer> filmIds);

    Film addFilm(Film film);

    Film updateFilm(Film film);

    Film deleteFilm(int filmId);

    List<Film> getMostLikedFilmsByGenreAndYear(int count, int genreID, int year);

    List<Film> searchFilm(String query, boolean directors, boolean tittle);

    List<Film> getCommonFriendFilms(int userId, int friendId);
}