package com.example.ANONIMUS;

import com.example.ANONIMUS.cache.CacheService;
import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.QrEntity;
import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.QrRepository;
import com.example.ANONIMUS.repository.UserRepository;
import com.example.ANONIMUS.service.QrService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrServiceTest {

    @Mock
    private QrRepository qrRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private QrDao qrDao;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private QrService qrService;

    private User testUser;
    private QrEntity testQr;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");

        testQr = new QrEntity();
        testQr.setContent("test");
        testQr.setQrCodeBase64("base64EncodedString");
    }

    @Test
    void generateAndSaveQrCode_ValidInput_ReturnsQrResponse() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(qrDao.generateQrCode("test")).thenReturn(new byte[100]);

        // Act
        QrResponse response = qrService.generateAndSaveQrCode("test", "testUser");

        // Assert
        assertNotNull(response);
        verify(qrRepository).save(any(QrEntity.class));
    }
}