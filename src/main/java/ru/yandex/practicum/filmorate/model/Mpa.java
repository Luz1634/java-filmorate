package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class Mpa {
    private final int id;
    @Size(max = 5)
    private final String name;
}
