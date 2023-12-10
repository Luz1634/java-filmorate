package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.DBGenreStorage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Component("dBFilmGenreStorage")
public class DBFilmGenreStorage {

    private final JdbcTemplate jdbcTemplate;

    private static final String SQL_INSERT_RELATION_GENRE_AND_FILM = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?);";

    @Autowired
    public DBFilmGenreStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Set<Genre> getFilmGenre(int filmId) {
        String sqlQuery = "SELECT g.genre_id, g.name FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?;";
        return jdbcTemplate.query(sqlQuery, DBGenreStorage::createGenre, filmId).stream()
                .sorted(Comparator.comparingInt(Genre::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<Integer, Set<Genre>> getFilmGenre(List<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = String.format("SELECT fg.film_id, g.genre_id, g.name FROM film_genres AS fg " +
                "JOIN genres AS g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id in (%s) " +
                "ORDER BY g.genre_id;", inSql);

        Map<Integer, Set<Genre>> result = films.stream().collect(Collectors.toMap(Film::getId, Film::getGenres));

        jdbcTemplate.query(sqlQuery, result.keySet().toArray(), (ResultSet rs) -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("genre_id"));
            genre.setName(rs.getString("name"));
            result.get(filmId).add(genre);
        });

        return result;
    }

    public void addFilmGenre(int filmId, int genreId) {
        jdbcTemplate.update(SQL_INSERT_RELATION_GENRE_AND_FILM, filmId, genreId);
    }

    public void addFilmGenre(int filmId, Set<Genre> genres) {
        jdbcTemplate.batchUpdate(SQL_INSERT_RELATION_GENRE_AND_FILM,
                genres,
                100,
                (PreparedStatement ps, Genre genre) -> {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genre.getId());
                });
    }

    public void deleteFilmGenre(int filmId, int genreId) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = ? AND genre_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    public void deleteFilmGenres(int filmId) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = ?;";
        jdbcTemplate.update(sqlQuery, filmId);
    }
}
