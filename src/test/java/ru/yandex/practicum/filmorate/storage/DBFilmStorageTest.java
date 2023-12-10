package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.DBFilmStorage;

import java.time.LocalDate;
import java.util.HashMap;

@JdbcTest
public class DBFilmStorageTest {
    private final JdbcTemplate jdbcTemplate;
    private final DBFilmStorage filmStorage;

    @Autowired
    public DBFilmStorageTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmStorage = new DBFilmStorage(jdbcTemplate);
    }

    @BeforeEach
    public void clearTable() {
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1;");
    }

    public Film createFilm(int id, String name, String description, int year, int month, int day,
                           int duration, Mpa mpa) {
        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(LocalDate.of(year, month, day));
        film.setDuration(duration);
        film.setMpa(mpa);
        return film;
    }

    public Film standardFilm() {
        return createFilm(1,
                "name 1",
                "description 1",
                2000,
                1,
                1,
                100,
                new Mpa(1, "G"));
    }

    public Film standardGetFilm() {
        return standardFilm();
    }

    public void fillFilms() {
        for (int i = 1; i <= 20; ++i) {
            filmStorage.addFilm(createFilm(0, "name +" + i, "description " + i, 2000, 1, i,
                    100 + i, new Mpa(1, "")));
        }
    }

    @Test
    public void getAllFilmsEmpty() {
        Assertions.assertEquals(new HashMap<>(), filmStorage.getAllFilms(),
                "Возвращается непустой список фильмов");
    }

    @Test
    public void getAllFilmsFill() {
        fillFilms();

        Assertions.assertEquals(20, filmStorage.getAllFilms().size(),
                "Возвращается неправильное количество фильмов");
    }

    @Test
    public void getExistFilm() {
        filmStorage.addFilm(standardFilm());

        Assertions.assertEquals(standardGetFilm(), filmStorage.getFilm(1),
                "Фильм с указанным id невозможно получить");
    }

    @Test
    public void getNonExistFilm() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> filmStorage.getFilm(1),
                "При добавлении фильма ожидалось UpdateNonExistObjectException");
    }

    @Test
    public void addFirstFilm() {
        Film film = standardFilm();
        film.setDirectors(null);
        Assertions.assertEquals(film, filmStorage.addFilm(film),
                "Первый созданный фильм неправильно добавляется");
        Assertions.assertEquals(standardGetFilm(), filmStorage.getFilm(1),
                "Первый созданный фильм неправильно добавляется");
    }

    @Test
    public void addNewFilm() {
        fillFilms();
        Film film = standardFilm();
        film.setId(0);

        Assertions.assertEquals(film, filmStorage.addFilm(film),
                "Новый фильм неправильно добавляется");

        film.setId(21);

        Assertions.assertEquals(film, filmStorage.getFilm(21),
                "Новый фильм неправильно добавляется");
    }

    @Test
    public void addExistFilm() {
        fillFilms();

        Assertions.assertThrows(AddExistObjectException.class,
                () -> filmStorage.addFilm(standardFilm()),
                "при добавлнении фильма ожидалось AddExistObjectException");
    }

    @Test
    public void addFilmWithoutOptionalParameters() {
        Film film = standardGetFilm();
        film.setDescription(null);

        Assertions.assertEquals(film, filmStorage.addFilm(film),
                "Необазятельные параметры фильма неправильно обрабатываются");

        film = standardGetFilm();
        film.setDescription("");

        Assertions.assertEquals(film, filmStorage.getFilm(1),
                "Необазятельные параметры фильма неправильно обрабатываются");
    }

    @Test
    public void addDuplicateFilm() {
        filmStorage.addFilm(standardFilm());

        Assertions.assertThrows(AddExistObjectException.class,
                () -> filmStorage.addFilm(standardFilm()),
                "При добавлении дубликата фильма ожидалось AddExistObjectException");
    }

    @Test
    public void updateExistFilm() {
        filmStorage.addFilm(standardFilm());
        Film film = createFilm(1, "2", "2", 2000, 2, 2,
                2, new Mpa(1, ""));

        Assertions.assertEquals(film, filmStorage.updateFilm(film),
                "Фильм неправильно обновляется");
    }

    @Test
    public void updateNonExistFilm() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> filmStorage.updateFilm(standardFilm()),
                "При обновлении фильма ожидалось UpdateNonExistObjectException");
    }

    @Test
    public void deleteExistFilm() {
        filmStorage.addFilm(standardFilm());

        Assertions.assertEquals(standardGetFilm(), filmStorage.deleteFilm(1),
                "Фильм неправильно удаляется");
    }

    @Test
    public void deleteNonExistFilm() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> filmStorage.deleteFilm(1),
                "При удалении фильма ожидалось UpdateNonExistObjectException");
    }
}
