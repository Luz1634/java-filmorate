package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component("dBDirectorStorage")
public class DBDirectorStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public DBDirectorStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Director> getAllDirectors() {
        String sqlQuery = "SELECT * FROM directors;";
        return jdbcTemplate.query(sqlQuery, DBDirectorStorage::createDirector);
    }

    @Override
    public Director getDirector(int directorID) {
        return checkContainsDirector(directorID);
    }

    @Override
    public Map<Integer, Set<Director>> getDirectorByFilm(List<Film> films) {
        String inSql = String.join(",", Collections.nCopies(films.size(), "?"));
        String sqlQuery = String.format("SELECT f.film_id, d.* " +
                "FROM films f " +
                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                "JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE f.film_id IN (%s);", inSql);

        Map<Integer, Set<Director>> result = films.stream().collect(Collectors.toMap(Film::getId, Film::getDirectors));

        jdbcTemplate.query(sqlQuery, result.keySet().toArray(), (ResultSet rs) -> {
            int filmId = rs.getInt("film_id");
            Director director = Director.builder()
                    .id(rs.getInt("director_id"))
                    .name(rs.getString("name"))
                    .build();
            result.get(filmId).add(director);
        });

        return result;
    }

    @Override
    public Director addDirector(Director director) {
        checkNonContainsDirector(director.getId());

        String sqlQuery = "INSERT INTO directors (name)" +
                "VALUES (?);";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sqlQuery, new String[]{"director_id"});
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId((int) Objects.requireNonNull(keyHolder.getKey()));

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        checkContainsDirector(director.getId());

        String sqlQuery = "UPDATE directors " +
                "SET name = ?" +
                "WHERE director_id = ?;";

        jdbcTemplate.update(sqlQuery,
                director.getName(),
                director.getId()
        );

        return director;
    }

    @Override
    public Director deleteDirector(int directorId) {
        Director director = checkContainsDirector(directorId);
        String sqlQuery = "DELETE FROM directors WHERE director_id = ?;";

        jdbcTemplate.update(sqlQuery, directorId);

        return director;
    }

    private void checkNonContainsDirector(int directorId) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = ?;";
        List<Director> director = jdbcTemplate.query(sqlQuery, DBDirectorStorage::createDirector, directorId);

        if (!director.isEmpty()) {
            throw new AddExistObjectException("Director с указанным id = " + directorId + " уже существует");
        }
    }

    public Director checkContainsDirector(int directorId) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id = ?;";
        List<Director> director = jdbcTemplate.query(sqlQuery, DBDirectorStorage::createDirector, directorId);

        if (director.size() != 1) {
            throw new UpdateNonExistObjectException("Director с указанным id = " + directorId + " не существует");
        }

        return director.get(0);
    }

    public static Director createDirector(ResultSet resultSet, int rowNum) throws SQLException {
        return Director.builder()
                .id(resultSet.getInt("director_id"))
                .name(resultSet.getString("name"))
                .build();
    }
}