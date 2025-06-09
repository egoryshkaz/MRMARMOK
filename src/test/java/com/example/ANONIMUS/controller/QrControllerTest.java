package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.dto.BulkQrRequest;
import com.example.ANONIMUS.dto.BulkQrResult;
import com.example.ANONIMUS.dto.QrGenerationRequest;
import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.QrEntity;
import com.example.ANONIMUS.service.QrService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(QrController.class)
public class QrControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrService qrService;

    // Вспомогательный метод для преобразования объектов в JSON
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void generateQrCodeValidRequestReturnsOk() throws Exception {
        QrResponse qrResponse = new QrResponse("dummyQrCodeBase64");
        when(qrService.generateAndSaveQrCode("test", "testuser")).thenReturn(qrResponse);

        mockMvc.perform(get("/api/qr")
                        .param("text", "test")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode", is("dummyQrCodeBase64")));
    }

    @Test
    public void generateQrCodeInvalidRequestReturnsBadRequest() throws Exception {
        // При пустых параметрах контроллер должен вернуть ошибку 400
        mockMvc.perform(get("/api/qr")
                        .param("text", "")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateBulkQrCodesValidRequestReturnsOk() throws Exception {
        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of(
                new QrGenerationRequest("text1", "testuser"),
                new QrGenerationRequest("text2", "testuser")
        ));

        // Подготовка мокированного ответа для bulk операции
        List<BulkQrResult> results = new ArrayList<>();
        results.add(BulkQrResult.success("text1", "testuser", "qrCode1"));
        results.add(BulkQrResult.success("text2", "testuser", "qrCode2"));
        when(qrService.generateBulkQrCodes(any())).thenReturn(results);

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].qrCodeBase64", is("qrCode1")));
    }

    @Test
    public void generateBulkQrCodesInvalidRequestReturnsBadRequest() throws Exception {
        // Пустой список запросов должен приводить к возврату ошибки 400
        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of());

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateBulkQrCodesWithInvalidItemReturnsOkWithMixedResults() throws Exception {
        BulkQrRequest request = new BulkQrRequest();
        request.setRequests(List.of(
                new QrGenerationRequest("valid", "testuser"),
                new QrGenerationRequest("", "")
        ));

        List<BulkQrResult> results = new ArrayList<>();
        results.add(BulkQrResult.success("valid", "testuser", "qrCodeValid"));
        results.add(BulkQrResult.failure("", "", "Invalid text or username"));
        when(qrService.generateBulkQrCodes(any())).thenReturn(results);

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].error").doesNotExist())
                .andExpect(jsonPath("$[1].error", is("Invalid text or username")));
    }

    @Test
    public void getQrCodesByUserValidUserReturnsQrCodes() throws Exception {
        // Подготовка мока для возврата списка QR-кодов по username
        List<QrEntity> qrCodes = new ArrayList<>();
        QrEntity code1 = new QrEntity();
        code1.setContent("first");
        QrEntity code2 = new QrEntity();
        code2.setContent("second");
        qrCodes.add(code1);
        qrCodes.add(code2);
        when(qrService.getQrCodesByUsername("testuser")).thenReturn(qrCodes);

        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].content", is("first")))
                .andExpect(jsonPath("$[1].content", is("second")));
    }

    @Test
    public void getQrCodesByUserInvalidUsernameReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getQrCodesByUserUserNotFoundReturnsEmptyList() throws Exception {
        when(qrService.getQrCodesByUsername("nonexistent")).thenReturn(List.of());

        mockMvc.perform(get("/api/qr/by-user")
                        .param("username", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", empty()));
    }
}
