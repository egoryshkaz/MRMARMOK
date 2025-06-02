package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User createUser(String username) {
        User user = new User();
        user.setUsername(username);
        return userRepository.save(user);
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() throws Exception {
        User newUser = new User();
        newUser.setUsername("testuser");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void getUserById_Exists_ReturnsUser() throws Exception {
        User user = createUser("testuser");

        mockMvc.perform(get("/api/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.id").value(user.getId()));
    }

    @Test
    void getUserById_NotExists_ReturnsNotFound() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_ValidId_UpdatesUser() throws Exception {
        User user = createUser("oldname");

        User updatedUser = new User();
        updatedUser.setUsername("newname");

        mockMvc.perform(put("/api/users/{id}", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newname"));
    }

    @Test
    void deleteUser_ValidId_DeletesUser() throws Exception {
        User user = createUser("todelete");

        mockMvc.perform(delete("/api/users/{id}", user.getId()))
                .andExpect(status().isNoContent());

        assertFalse(userRepository.existsById(user.getId()));
    }

    @Test
    void getAllUsers_ReturnsAllUsers() throws Exception {
        createUser("user1");
        createUser("user2");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("user1")))
                .andExpect(jsonPath("$[1].username", is("user2")));
    }

    @Test
    void createUser_InvalidUsername_ReturnsBadRequest() throws Exception {
        User invalidUser = new User();
        invalidUser.setUsername(""); // Пустое имя

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidUser)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_NonExistingId_ReturnsNotFound() throws Exception {
        User updatedUser = new User();
        updatedUser.setUsername("newname");

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_DuplicateUsername_ReturnsConflict() throws Exception {
        createUser("duplicate");

        User duplicateUser = new User();
        duplicateUser.setUsername("duplicate");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(duplicateUser)))
                .andExpect(status().isConflict());
    }
}