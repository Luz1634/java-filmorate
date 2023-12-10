package ru.yandex.practicum.filmorate.model.enumerations;

import ru.yandex.practicum.filmorate.exception.exceptions.NoSuchEnumException;

public enum SortType {
    LIKES,
    YEAR;

    public static SortType fromStringIgnoreCase(String data) {
        if (data != null) {
            for (SortType sortType : SortType.values()) {
                if (data.equalsIgnoreCase(sortType.toString())) {
                    return sortType;
                }
            }
        }
        throw new NoSuchEnumException(String.format("Не найдено перечисление для значения %s", data));
    }
}
