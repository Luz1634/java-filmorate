package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

class FilmControllerTest {
    public FilmController filmController;

    @BeforeEach
    public void createNewFilmController() {
        filmController = new FilmController();
    }

    public Film createFilm() {
        Film film = new Film();
        film.setId(1);
        film.setName("Name");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1967, 3, 27));
        film.setDuration(150);
        return film;
    }

    //Method Tests
    @Test
    public void createCorrectFilm() {
        Assertions.assertEquals(200, filmController.addFilm(createFilm()).getStatusCodeValue());
        Assertions.assertEquals(createFilm(), filmController.getAllFilms().getBody().get(0));
    }


    @Test
    public void createCorrectFilmTwice() {
        Assertions.assertEquals(200, filmController.addFilm(createFilm()).getStatusCodeValue());
        Assertions.assertEquals(400, filmController.addFilm(createFilm()).getStatusCodeValue());
    }

    @Test
    public void updateCorrectFilm() {
        Film film = createFilm();
        filmController.addFilm(film);
        film.setName("newName");

        Assertions.assertEquals(200, filmController.updateFilm(film).getStatusCodeValue());
    }

    @Test
    public void updateFilmFailId() {
        Film film = createFilm();
        filmController.addFilm(film);
        film.setName("newName");
        film.setId(-1);

        Assertions.assertEquals(404, filmController.updateFilm(film).getStatusCodeValue());
    }

    @Test
    public void updateFailFilm() {
        Film film = createFilm();
        filmController.addFilm(film);
        film.setName("");

        Assertions.assertEquals(400, filmController.updateFilm(film).getStatusCodeValue());
    }

    @Test
    public void getEmptyAllFilms() {
        Assertions.assertEquals(200, filmController.getAllFilms().getStatusCodeValue());
        Assertions.assertEquals(0, filmController.getAllFilms().getBody().size());
    }

    @Test
    public void getExistAllFilms() {
        Film film = createFilm();
        filmController.addFilm(film);
        film.setId(0);
        film.setName("neqName");
        film.setDescription("description 1234");
        film.setDuration(200);
        filmController.addFilm(film);

        Assertions.assertEquals(200, filmController.getAllFilms().getStatusCodeValue());
        Assertions.assertEquals(2, filmController.getAllFilms().getBody().size());
    }


    //Validation Tests
    @Test
    public void createEmptyFilm() {
        Film film = new Film();

        Assertions.assertEquals(400, filmController.addFilm(film).getStatusCodeValue());
    }

    @Test
    public void createFilmEmptyName() {
        Film film = createFilm();
        film.setName(" ");

        Assertions.assertEquals(400, filmController.addFilm(film).getStatusCodeValue());
    }

    @Test
    public void createFilmDescriptionMoreThan200Char() {
        Film film = createFilm();
        film.setDescription("description ".repeat(100));

        Assertions.assertEquals(400, filmController.addFilm(film).getStatusCodeValue());
    }

    @Test
    public void createFilmFailReleaseDate() {
        Film film = createFilm();
        film.setReleaseDate(LocalDate.of(1000, 5, 17));

        Assertions.assertEquals(400, filmController.addFilm(film).getStatusCodeValue());
    }

    @Test
    public void createFilmNegativeDuration() {
        Film film = createFilm();
        film.setDuration(-100);

        Assertions.assertEquals(400, filmController.addFilm(film).getStatusCodeValue());
    }


    //Change null description to empty string Tests
    @Test
    public void createFilmNullDescription() {
        Film film = createFilm();
        film.setDescription(null);

        Assertions.assertEquals(200, filmController.addFilm(film).getStatusCodeValue());

        film.setDescription("");

        Assertions.assertEquals(film, filmController.getAllFilms().getBody().get(0));
    }
}