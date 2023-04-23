package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

class UserControllerTest {
    public UserController userController;

    @BeforeEach
    public void createNewUserController() {
        userController = new UserController();
    }

    public User createUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("e@mail.tu");
        user.setLogin("Login");
        user.setName("Name");
        user.setBirthday(LocalDate.of(1967, 3, 27));
        return user;
    }


    //Method Tests
    @Test
    public void createCorrectUser() {
        Assertions.assertEquals(200, userController.addUser(createUser()).getStatusCodeValue());
        Assertions.assertEquals(createUser(), userController.getAllUsers().getBody().get(0));
    }


    @Test
    public void createCorrectUserTwice() {
        Assertions.assertEquals(200, userController.addUser(createUser()).getStatusCodeValue());
        Assertions.assertEquals(400, userController.addUser(createUser()).getStatusCodeValue());
    }

    @Test
    public void updateCorrectUser() {
        User user = createUser();
        userController.addUser(user);
        user.setName("newName");

        Assertions.assertEquals(200, userController.updateUser(user).getStatusCodeValue());
    }

    @Test
    public void updateUserFailId() {
        User user = createUser();
        userController.addUser(user);
        user.setName("newName");
        user.setId(-1);

        Assertions.assertEquals(404, userController.updateUser(user).getStatusCodeValue());
    }

    @Test
    public void updateFailUser() {
        User user = createUser();
        userController.addUser(user);
        user.setLogin("l o g i n");

        Assertions.assertEquals(400, userController.updateUser(user).getStatusCodeValue());
    }

    @Test
    public void getEmptyAllUsers() {
        Assertions.assertEquals(200, userController.getAllUsers().getStatusCodeValue());
        Assertions.assertEquals(0, userController.getAllUsers().getBody().size());
    }

    @Test
    public void getExistAllUsers() {
        User user = createUser();
        userController.addUser(user);
        user.setId(0);
        user.setEmail("nnn@mail.me");
        user.setName("ad");
        user.setLogin("log");
        userController.addUser(user);

        Assertions.assertEquals(200, userController.getAllUsers().getStatusCodeValue());
        Assertions.assertEquals(2, userController.getAllUsers().getBody().size());
    }


    //Validation Tests
    @Test
    public void createEmptyUser() {
        User user = new User();

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }

    @Test
    public void createUserFailEmail() {
        User user = createUser();
        user.setEmail("email");

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }

    @Test
    public void createUserEmptyEmail() {
        User user = createUser();
        user.setEmail("");

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }

    @Test
    public void createUserFailLogin() {
        User user = createUser();
        user.setLogin("log in");

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }

    @Test
    public void createUserEmptyLogin() {
        User user = createUser();
        user.setLogin("");

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }

    @Test
    public void createUserFutureBirthday() {
        User user = createUser();
        user.setBirthday(LocalDate.of(3000, 5, 17));

        Assertions.assertEquals(400, userController.addUser(user).getStatusCodeValue());
    }


    //Change name to login Tests
    @Test
    public void createUserEmptyName() {
        User user = createUser();
        user.setName("");

        Assertions.assertEquals(200, userController.addUser(user).getStatusCodeValue());

        user.setName(user.getLogin());

        Assertions.assertEquals(user, userController.getAllUsers().getBody().get(0));
    }

    @Test
    public void createUserNullName() {
        User user = createUser();
        user.setName(null);

        Assertions.assertEquals(200, userController.addUser(user).getStatusCodeValue());

        user.setName(user.getLogin());

        Assertions.assertEquals(user, userController.getAllUsers().getBody().get(0));
    }
}