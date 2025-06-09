package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Зависимость, которую контроллер использует, замокана
    @MockitoBean
    private UserService userService;

    // Вспомогательный метод для преобразования объектов в JSON
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void createUserValidReturnsCreatedUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("testuser");
        newUser.setId(1L);

        when(userService.createUser(any(User.class))).thenReturn(newUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    public void getUserByIdExistsReturnsUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    public void getUserByIdNotExistsReturnsNotFound() throws Exception {
        when(userService.getUserById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void updateUserValidUpdatesUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setUsername("newname");

        when(userService.updateUser(eq(1L), any(User.class)))
                .thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newname")));
    }

    @Test
    public void deleteUserValidDeletesUser() throws Exception {
        // Метод deleteUser в UserService возвращает void. Если исключения нет, то запрос успешно выполняется.
        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getAllUsersReturnsAllUsers() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    public void createUserInvalidUsernameReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setUsername(""); // Пустое имя

        // Сервис выбрасывает исключение, которое глобальный обработчик интерпретирует как BAD_REQUEST
        when(userService.createUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Invalid username"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void updateUserNonExistingIdReturnsNotFound() throws Exception {
        User updatedUser = new User();
        updatedUser.setUsername("newname");

        // Сервис для обновления не находит пользователя и выбрасывает исключение, которое должно быть обработано глобально как NOT_FOUND
        when(userService.updateUser(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUserDuplicateUsernameReturnsConflict() throws Exception {
        User duplicateUser = new User();
        duplicateUser.setUsername("duplicate");

        // Сервис выбрасывает исключение для дублирования, которое должно обработаться как CONFLICT
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Duplicate username"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(duplicateUser)))
                .andExpect(status().isConflict());
    }
}
