package ru.yandex.practicum.filmorate.model;

import java.time.LocalDate;

@lombok.Data
public class Film {
    private int id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private int duration;
}