package com.example.ANONIMUS;

import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.service.QrService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class QrControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private QrService qrService;

    @Test
    void generateQrCode_WhenValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String testText = "test";
        String username = "user";
        QrResponse mockResponse = new QrResponse("base64String");

        when(qrService.generateAndSaveQrCode(testText, username))
                .thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(get("/api/qr")
                        .param("text", testText)
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.qrCode").value("base64String"));
    }

    @Test
    void generateQrCode_WhenMissingParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/qr"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void generateBulkQrCodes_WhenValidRequest_ShouldReturnOk() throws Exception {
        // Arrange
        String jsonRequest = """
            {
                "requests": [
                    {"text": "test1", "username": "user1"},
                    {"text": "test2", "username": "user2"}
                ]
            }""";

        // Act & Assert
        mockMvc.perform(post("/api/qr/bulk")
                        .contentType("application/json")
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }
}