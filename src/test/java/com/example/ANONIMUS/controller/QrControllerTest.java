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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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

    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void generateQrCodeValidRequestReturnsOk() throws Exception {
        // Создаём объект QrResponse через мок
        QrResponse qrResponse = mock(QrResponse.class);
        when(qrResponse.getQrCode()).thenReturn("dummyQrCodeBase64");
        when(qrService.generateAndSaveQrCode("test", "testuser")).thenReturn(qrResponse);

        mockMvc.perform(get("/api/qr")
                        .param("text", "test")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode", is("dummyQrCodeBase64")));
    }

    @Test
    public void generateQrCodeInvalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/qr")
                        .param("text", "")
                        .param("username", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateBulkQrCodesValidRequestReturnsOk() throws Exception {
        BulkQrRequest bulkRequest = mock(BulkQrRequest.class);
        QrGenerationRequest qrGenReq1 = mock(QrGenerationRequest.class);
        QrGenerationRequest qrGenReq2 = mock(QrGenerationRequest.class);
        when(qrGenReq1.getText()).thenReturn("text1");
        when(qrGenReq1.getUsername()).thenReturn("testuser");
        when(qrGenReq2.getText()).thenReturn("text2");
        when(qrGenReq2.getUsername()).thenReturn("testuser");
        when(bulkRequest.getRequests()).thenReturn(List.of(qrGenReq1, qrGenReq2));

        BulkQrResult bulkResult1 = mock(BulkQrResult.class);
        BulkQrResult bulkResult2 = mock(BulkQrResult.class);
        when(bulkResult1.getQrCodeBase64()).thenReturn("qrCode1");
        when(bulkResult2.getQrCodeBase64()).thenReturn("qrCode2");
        List<BulkQrResult> results = List.of(bulkResult1, bulkResult2);
        when(qrService.generateBulkQrCodes(any())).thenReturn(results);

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bulkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].qrCodeBase64", is("qrCode1")));
    }

    @Test
    public void generateBulkQrCodesInvalidRequestReturnsBadRequest() throws Exception {
        // Здесь объект BulkQrRequest возвращает пустой список через мок
        BulkQrRequest bulkRequest = mock(BulkQrRequest.class);
        when(bulkRequest.getRequests()).thenReturn(List.of());

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bulkRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void generateBulkQrCodesWithInvalidItemReturnsOkWithMixedResults() throws Exception {
        BulkQrRequest bulkRequest = mock(BulkQrRequest.class);
        QrGenerationRequest validReq = mock(QrGenerationRequest.class);
        QrGenerationRequest invalidReq = mock(QrGenerationRequest.class);
        when(validReq.getText()).thenReturn("valid");
        when(validReq.getUsername()).thenReturn("testuser");
        when(invalidReq.getText()).thenReturn("");
        when(invalidReq.getUsername()).thenReturn("");
        when(bulkRequest.getRequests()).thenReturn(List.of(validReq, invalidReq));

        BulkQrResult successResult = mock(BulkQrResult.class);
        BulkQrResult failureResult = mock(BulkQrResult.class);
        when(successResult.getQrCodeBase64()).thenReturn("qrCodeValid");
        when(failureResult.getError()).thenReturn("Invalid text or username");
        List<BulkQrResult> results = List.of(successResult, failureResult);
        when(qrService.generateBulkQrCodes(any())).thenReturn(results);

        mockMvc.perform(post("/api/qr/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bulkRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].error", not(exists())))
                .andExpect(jsonPath("$[1].error", is("Invalid text or username")));
    }

    @Test
    public void getQrCodesByUserValidUserReturnsQrCodes() throws Exception {
        // Создаём моки для QR-кодов
        QrEntity code1 = mock(QrEntity.class);
        QrEntity code2 = mock(QrEntity.class);
        when(code1.getContent()).thenReturn("first");
        when(code2.getContent()).thenReturn("second");
        List<QrEntity> qrCodes = List.of(code1, code2);

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
