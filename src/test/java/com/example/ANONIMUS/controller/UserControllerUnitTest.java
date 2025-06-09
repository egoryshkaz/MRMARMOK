package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserValidUserReturnsCreatedUser() {
        // Подготовка
        User newUser = new User();
        newUser.setUsername("testuser");

        when(userService.createUser(any(User.class)))
                .thenReturn(newUser);

        // Вызов
        ResponseEntity<User> response = userController.createUser(newUser);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getUserByIdExistsReturnsUser() {
        // Подготовка
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userService.getUserById(1L))
                .thenReturn(Optional.of(user));

        // Вызов
        ResponseEntity<User> response = userController.getUserById(1L);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getUserByIdNotExistsReturnsNotFound() {
        // Подготовка
        when(userService.getUserById(1L))
                .thenReturn(Optional.empty());

        // Вызов
        ResponseEntity<User> response = userController.getUserById(1L);

        // Проверка
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateUserValidIdReturnsUpdatedUser() {
        // Подготовка
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("oldname");

        User updatedUser = new User();
        updatedUser.setUsername("newname");

        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(updatedUser);

        // Вызов
        ResponseEntity<User> response = userController.updateUser(1L, updatedUser);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newname", response.getBody().getUsername());
    }

    @Test
    void deleteUserValidIdReturnsNoContent() {
        // Вызов
        ResponseEntity<Void> response = userController.deleteUser(1L);

        // Проверка
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getAllUsersReturnsAllUsers() {
        // Подготовка
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(userService.getAllUsers())
                .thenReturn(Arrays.asList(user1, user2));

        // Вызов
        ResponseEntity<List<User>> response = userController.getAllUsers();

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getUsername());
    }

    @Test
    void updateUserNonExistingIdThrowsException() {
        // Подготовка
        when(userService.updateUser(eq(99L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        // Вызов и проверка
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            userController.updateUser(99L, new User());
        });

        assertEquals("User not found", exception.getMessage());
    }
}
