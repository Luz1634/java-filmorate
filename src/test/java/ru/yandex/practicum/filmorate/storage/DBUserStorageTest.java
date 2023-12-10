package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.exceptions.AddExistObjectException;
import ru.yandex.practicum.filmorate.exception.exceptions.UpdateNonExistObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.DBUserStorage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;

@JdbcTest
public class DBUserStorageTest {

    private final JdbcTemplate jdbcTemplate;
    private final DBUserStorage userStorage;

    @Autowired
    public DBUserStorageTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userStorage = new DBUserStorage(jdbcTemplate);
    }

    @BeforeEach
    public void setFilmStorage() {
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1;");
    }

    public User createUser(int id, String email, String login, String name, int year, int month, int day) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setLogin(login);
        user.setName(name);
        user.setBirthday(LocalDate.of(year, month, day));
        return user;
    }

    public User standardUser() {
        return createUser(1,
                "e1@mail.ru",
                "login1",
                "name 1",
                2000,
                1,
                1);
    }

    public User standardUserNotNullFriends() {
        User user = standardUser();
        user.setFriends(new HashSet<>());
        return user;
    }

    public void fillUsers() {
        for (int i = 1; i <= 20; ++i) {
            userStorage.addUser(createUser(0, "em" + i + "@mail.ru", "loginn" + i,
                    "name " + i, 2000, 1, i));
        }
    }

    @Test
    public void getAllUsersEmpty() {
        Assertions.assertEquals(new HashMap<>(), userStorage.getAllUsers(),
                "Возвращается непустой список пользователей");
    }

    @Test
    public void getAllUsersFill() {
        fillUsers();

        Assertions.assertEquals(20, userStorage.getAllUsers().size(),
                "Возвращается неправильное количество пользователей");
    }

    @Test
    public void getExistUser() {
        userStorage.addUser(standardUser());

        Assertions.assertEquals(standardUser(), userStorage.getUser(1),
                "Пользователь с указанным id невозможно получить");
    }

    @Test
    public void getNonExistUser() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> userStorage.getUser(1),
                "при добавлении пользователя ожидалось UpdateNonExistObjectException");
    }

    @Test
    public void addFirstUser() {

        Assertions.assertEquals(standardUserNotNullFriends(), userStorage.addUser(standardUser()),
                "Первый созданный пользователь неправильно добавляется");
        Assertions.assertEquals(standardUser(), userStorage.getUser(1),
                "Первый созданный пользователь неправильно добавляется");
    }

    @Test
    public void addNewUser() {
        fillUsers();
        User user = standardUser();
        user.setId(0);

        Assertions.assertEquals(user, userStorage.addUser(user),
                "Новый пользователь неправильно добавляется");

        user.setId(21);
        user.setFriends(null);

        Assertions.assertEquals(user, userStorage.getUser(21),
                "Новый пользователь неправильно добавляется");
    }

    @Test
    public void addExistUser() {
        fillUsers();

        Assertions.assertThrows(AddExistObjectException.class,
                () -> userStorage.addUser(standardUser()),
                "При добавлнении пользователя ожидалось AddExistObjectException");
    }

    @Test
    public void addUserWithoutOptionalParameters() {
        User user = standardUserNotNullFriends();
        user.setName(null);

        Assertions.assertEquals(user, userStorage.addUser(user),
                "Необазятельные параметры пользователя неправильно обрабатываются");

        user = standardUser();
        user.setName(user.getLogin());

        Assertions.assertEquals(user, userStorage.getUser(1),
                "Необазятельные параметры пользователя неправильно обрабатываются");
    }

    @Test
    public void addDuplicateUser() {
        userStorage.addUser(standardUser());

        Assertions.assertThrows(AddExistObjectException.class,
                () -> userStorage.addUser(standardUser()),
                "При добавлении дубликата пользователя ожидалось AddExistObjectException");
    }

    @Test
    public void updateExistUser() {
        userStorage.addUser(standardUser());
        User user = createUser(1, "e" + 2 + "@mail.ru", "login" + 2,
                "name " + 2, 2000, 1, 2);

        Assertions.assertEquals(user, userStorage.updateUser(user),
                "Пользователь неправильно обновляется");
    }

    @Test
    public void updateNonExistUser() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> userStorage.updateUser(standardUser()),
                "При обновлении пользователя ожидалось UpdateNonExistObjectException");
    }

    @Test
    public void deleteExistUser() {
        User user = standardUser();
        userStorage.addUser(user);

        Assertions.assertEquals(user, userStorage.deleteUser(1),
                "Пользователь неправильно удаляется");
    }

    @Test
    public void deleteNonExistUser() {
        Assertions.assertThrows(UpdateNonExistObjectException.class,
                () -> userStorage.deleteUser(1),
                "При удалении пользователя ожидалось UpdateNonExistObjectException");
    }
}
