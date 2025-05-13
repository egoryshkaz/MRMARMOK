package com.example.ANONIMUS;

import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.service.QrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
class QrControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private QrService qrService;

    @Test
    void generateQrCode_ValidRequest_ReturnsOk() throws Exception {
        when(qrService.generateAndSaveQrCode("test", "user"))
                .thenReturn(new QrResponse("base64String"));

        mockMvc.perform(get("/api/qr")
                        .param("text", "test")
                        .param("username", "user"))
                .andExpect(status().isOk());
    }
}
