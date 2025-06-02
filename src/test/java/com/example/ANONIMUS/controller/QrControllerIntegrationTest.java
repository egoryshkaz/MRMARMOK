package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.dto.BulkQrRequest;
import com.example.ANONIMUS.dto.QrGenerationRequest;
import com.example.ANONIMUS.model.QrEntity;
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

import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class QrControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    // Вспомогательный метод для преобразования объектов в JSON
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generateQrCode_ValidRequest_ReturnsOk() throws Exception {
        // Подготовка тестовых данных
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        // Выполнение и проверка запроса
        mockMvc.perform(get("/api/qr")
                        .param("text", "test")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").exists());
    }

    @Test
    void generateQrCode_InvalidRequest_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/qr")
                        .param("text", "")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateBulkQrCodes_ValidRequest_ReturnsOk() throws Exception {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of(
                new QrGenerationRequest("text1", "testuser"),
                new QrGenerationRequest("text2", "testuser")
        ));

        // Выполнение
        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].qrCodeBase64", notNullValue()));
    }

    @Test
    void generateBulkQrCodes_InvalidRequest_ReturnsBadRequest() throws Exception {
        // Пустой запрос
        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(Collections.emptyList());

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateBulkQrCodes_WithInvalidItem_ReturnsOkWithMixedResults() throws Exception {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of(
                new QrGenerationRequest("valid", "testuser"),
                new QrGenerationRequest("", "") // невалидный элемент
        ));

        // Выполнение
        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].error").doesNotExist())
                .andExpect(jsonPath("$[1].error").exists());
    }

    @Test
    void getQrCodesByUser_ValidUser_ReturnsQrCodes() throws Exception {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        // Добавляем QR-код пользователю
        mockMvc.perform(get("/api/qr")
                .param("text", "first")
                .param("username", "testuser"));

        mockMvc.perform(get("/api/qr")
                .param("text", "second")
                .param("username", "testuser"));

        // Получение QR-кодов
        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("first")))
                .andExpect(jsonPath("$[1].content", is("second")));
    }

    @Test
    void getQrCodesByUser_InvalidUsername_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getQrCodesByUser_UserNotFound_ReturnsEmptyList() throws Exception {
        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }

    // Новые тесты для edge cases
    @Test
    void generateQrCode_UserDoesNotExist_CreatesNewUser() throws Exception {
        mockMvc.perform(get("/api/qr")
                        .param("text", "test")
                        .param("username", "newuser"))
                .andExpect(status().isOk());

        // Проверяем, что пользователь создан
        assertTrue(userRepository.findByUsername("newuser").isPresent());
    }

    @Test
    void generateBulkQrCodes_LargeBatch_ProcessesAll() throws Exception {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        BulkQrRequest request = new BulkQrRequest();
        List<QrGenerationRequest> requests = new java.util.ArrayList<>();
        for (int i = 0; i < 50; i++) {
            requests.add(new QrGenerationRequest("text" + i, "testuser"));
        }
        request.setRequests(requests);

        // Выполнение
        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(50)));
    }

    @Test
    void generateBulkQrCodes_PartialInvalid_ProcessesValid() throws Exception {
        // Подготовка
        User user = new User();
        user.setUsername("testuser");
        userRepository.save(user);

        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of(
                new QrGenerationRequest("valid1", "testuser"),
                new QrGenerationRequest("", "testuser"), // невалидный text
                new QrGenerationRequest("valid2", "testuser"),
                new QrGenerationRequest("invalid", "")   // невалидный username
        ));

        // Выполнение
        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[0].error").doesNotExist())
                .andExpect(jsonPath("$[1].error").exists())
                .andExpect(jsonPath("$[2].error").doesNotExist())
                .andExpect(jsonPath("$[3].error").exists());
    }
}