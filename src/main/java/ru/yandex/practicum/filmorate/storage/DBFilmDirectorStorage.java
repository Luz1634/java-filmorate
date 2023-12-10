package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.enumerations.SortType;
import ru.yandex.practicum.filmorate.storage.director.DBDirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.DBFilmStorage;

import java.sql.PreparedStatement;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("dBFilmDirectorStorage")
public class DBFilmDirectorStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final String SQL_INSERT_RELATION_DIRECTOR_AND_FILM = "INSERT INTO film_directors (film_id, director_id) " +
            "VALUES (?, ?);";

    @Autowired
    public DBFilmDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Director> getFilmDirector(int filmId) {
        String sqlQuery = "SELECT d.director_id, d.name FROM film_directors AS fd " +
                "JOIN directors AS d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = ?;";
        return jdbcTemplate.query(sqlQuery, DBDirectorStorage::createDirector, filmId).stream()
                .sorted(Comparator.comparingInt(Director::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public List<Film> getDirectorFilms(int directorId, SortType sort) {
        String sqlQuery;
        String sqlQueryOrderYear = "SELECT *,m.name AS mpa_name FROM films AS f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                "WHERE fd.director_id = ? " +
                "ORDER BY EXTRACT(YEAR FROM f.release_date) ASC;";
        String sqlQueryOrderLikes = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS total_likes FROM films f JOIN film_directors fd " +
                "ON fd.film_id = f.film_id JOIN directors d ON d.director_id = fd.director_id LEFT JOIN  likes l " +
                "ON l.film_id = f.film_id JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE d.director_id = ? " +
                "GROUP BY f.film_id, d.director_id " +
                "ORDER BY total_likes DESC;";

        if (sort.equals(SortType.LIKES)) {
            sqlQuery = sqlQueryOrderLikes;
        } else {
            sqlQuery = sqlQueryOrderYear;
        }
        return jdbcTemplate.query(sqlQuery, DBFilmStorage::createFilm, directorId);
    }

    public void addFilmDirector(int filmId, int directorId) {
        jdbcTemplate.update(SQL_INSERT_RELATION_DIRECTOR_AND_FILM, filmId, directorId);
    }

    public void addFilmDirector(int filmId, Set<Director> directors) {
        jdbcTemplate.batchUpdate(SQL_INSERT_RELATION_DIRECTOR_AND_FILM,
                directors,
                100,
                (PreparedStatement ps, Director director) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, director.getId());
                });
    }

    public void deleteFilmDirector(int filmId, int directorId) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id = ? AND director_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId, directorId);
    }

    public void deleteFilmDirectors(int filmId) {
        String sqlQuery = "DELETE FROM film_directors WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId);
    }
}
