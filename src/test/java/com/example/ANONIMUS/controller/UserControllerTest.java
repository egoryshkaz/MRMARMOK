package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;  // Обратите внимание, используем @MockBean
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
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

    @MockBean
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
        // Создаём моки для пользователя, вместо new User()
        User newUser = mock(User.class);
        when(newUser.getUsername()).thenReturn("testuser");
        when(newUser.getId()).thenReturn(1L);

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
        User user = mock(User.class);
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");

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
        User updatedUser = mock(User.class);
        when(updatedUser.getUsername()).thenReturn("newname");
        when(updatedUser.getId()).thenReturn(1L);

        when(userService.updateUser(eq(1L), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newname")));
    }

    @Test
    public void deleteUserValidDeletesUser() throws Exception {

        mockMvc.perform(delete("/api/users/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    public void getAllUsersReturnsAllUsers() throws Exception {
        User user1 = mock(User.class);
        when(user1.getId()).thenReturn(1L);
        when(user1.getUsername()).thenReturn("user1");

        User user2 = mock(User.class);
        when(user2.getId()).thenReturn(2L);
        when(user2.getUsername()).thenReturn("user2");

        when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    public void createUserInvalidUsernameReturnsBadRequest() throws Exception {
        User invalidUser = mock(User.class);
        // Пустое имя, как требовалось
        when(invalidUser.getUsername()).thenReturn("");

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
        User updatedUser = mock(User.class);
        when(updatedUser.getUsername()).thenReturn("newname");

        // Сервис не находит пользователя и выбрасывает исключение (будет обработано как NOT_FOUND)
        when(userService.updateUser(eq(999L), any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(put("/api/users/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createUserDuplicateUsernameReturnsConflict() throws Exception {
        User duplicateUser = mock(User.class);
        when(duplicateUser.getUsername()).thenReturn("duplicate");

        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Duplicate username"));

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(duplicateUser)))
                .andExpect(status().isConflict());
    }



    @Test
    public void getUserByIdNullParameterReturnsNotFound() throws Exception {
        // Если id равен 0, считаем, что пользователь не найден
        when(userService.getUserById(0L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", 0L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void deleteUserNonExistingIdReturnsNotFound() throws Exception {
        // Допустим, при удалении несуществующего пользователя сервис выбрасывает исключение
        when(userService.deleteUser(eq(999L))).thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(delete("/api/users/{id}", 999L))
                .andExpect(status().isNotFound());
    }
}
