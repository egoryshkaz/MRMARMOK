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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUserValidReturnsCreatedUser() {
        // Создаём мока пользователя вместо new User()
        User newUser = mock(User.class);
        when(newUser.getUsername()).thenReturn("testuser");
        when(newUser.getId()).thenReturn(1L);

        when(userService.createUser(any(User.class))).thenReturn(newUser);

        ResponseEntity<User> response = userController.createUser(newUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getUserByIdExistsReturnsUser() {
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");

        when(userService.getUserById(eq(1L))).thenReturn(Optional.of(user));

        ResponseEntity<User> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("testuser", response.getBody().getUsername());
    }

    @Test
    void getUserByIdNotExistsReturnsNotFound() {
        when(userService.getUserById(eq(1L))).thenReturn(Optional.empty());

        ResponseEntity<User> response = userController.getUserById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateUserValidIdReturnsUpdatedUser() {
        User existingUser = mock(User.class);
        when(existingUser.getId()).thenReturn(1L);
        when(existingUser.getUsername()).thenReturn("oldname");

        User updatedUser = mock(User.class);
        when(updatedUser.getUsername()).thenReturn("newname");

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        ResponseEntity<User> response = userController.updateUser(1L, updatedUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("newname", response.getBody().getUsername());
    }

    @Test
    void deleteUserValidIdReturnsNoContent() {
        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(1L);
    }

    @Test
    void getAllUsersReturnsAllUsers() {
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(1L);
        when(user1.getUsername()).thenReturn("user1");

        User user2 = mock(User.class);
        when(user2.getId()).thenReturn(2L);
        when(user2.getUsername()).thenReturn("user2");

        when(userService.getAllUsers()).thenReturn(Arrays.asList(user1, user2));

        ResponseEntity<List<User>> response = userController.getAllUsers();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("user1", response.getBody().get(0).getUsername());
    }

    @Test
    void updateUserNonExistingIdThrowsException() {
        User updatedUser = mock(User.class);
        when(updatedUser.getUsername()).thenReturn("newname");

        when(userService.updateUser(eq(99L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userController.updateUser(99L, updatedUser)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void createUserNullInputThrowsException() {
        when(userService.createUser(null))
                .thenThrow(new IllegalArgumentException("Input user is null"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                userController.createUser(null)
        );
        assertEquals("Input user is null", exception.getMessage());
    }

    @Test
    void deleteUserNonExistingIdReturnsNotFound() {
        doThrow(new RuntimeException("User not found")).when(userService).deleteUser(eq(999L));

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userController.deleteUser(999L)
        );
        assertEquals("User not found", exception.getMessage());
    }
}
