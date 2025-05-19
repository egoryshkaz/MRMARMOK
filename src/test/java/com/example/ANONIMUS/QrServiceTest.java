package com.example.ANONIMUS;

import com.example.ANONIMUS.cache.CacheService;
import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.dto.BulkQrResult;
import com.example.ANONIMUS.dto.QrGenerationRequest;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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


    @Test
    void getQrById_WhenNotCached_ShouldFetchFromDb() {
        when(cacheService.containsKey("qr_1")).thenReturn(false);
        when(qrRepository.findById(1L)).thenReturn(Optional.of(testQr));

        QrEntity result = qrService.getQrById(1L);

        verify(cacheService).put(eq("qr_1"), any());
        assertEquals(testQr, result);
    }
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testUser");

        testQr = new QrEntity();
        testQr.setContent("test");
        testQr.setQrCodeBase64("base64EncodedString");
    }

    @Test
    void generateAndSaveQrCode_WhenValidInput_ShouldReturnQrResponse() {
        // Arrange
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));
        when(qrDao.generateQrCode("test")).thenReturn(new byte[100]);

        // Act
        QrResponse response = qrService.generateAndSaveQrCode("test", "testUser");

        // Assert
        assertNotNull(response);
        verify(qrRepository).save(any(QrEntity.class));
    }
    @Test
    void generateBulkQrCodes_WhenValidRequests_ShouldReturnsSuccessResults() {
        // Arrange (подготовка данных)
        List<QrGenerationRequest> requests = Arrays.asList(
                new QrGenerationRequest("text1", "testUser1"),
                new QrGenerationRequest("text2", "testUser2")
        );

        User user1 = new User();
        user1.setUsername("testUser1");
        User user2 = new User();
        user2.setUsername("testUser2");

        // Настройка поведения моков
        when(userRepository.findByUsername("testUser1")).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername("testUser2")).thenReturn(Optional.of(user2));
        when(qrDao.generateQrCode(anyString())).thenReturn(new byte[100]); // Симуляция QR-кода
        when(qrRepository.save(any(QrEntity.class))).thenAnswer(invocation -> {
            QrEntity qr = invocation.getArgument(0);
            qr.setId(1L); // Симуляция сохранения с присвоением ID
            return qr;
        });

        // Act (выполнение метода)
        List<BulkQrResult> results = qrService.generateBulkQrCodes(requests);

        // Assert (проверка результата)
        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(result -> result.getQrCodeBase64() != null));
        verify(userRepository, times(2)).findByUsername(anyString());
        verify(qrRepository, times(2)).save(any(QrEntity.class));
    }
    @Test
    void generateBulkQrCodes_WhenInvalidRequests_ShouldReturnsFailureResults() {
        // Arrange
        List<QrGenerationRequest> requests = Arrays.asList(
                new QrGenerationRequest(null, "testUser"), // Пустой текст
                new QrGenerationRequest("text", null)      // Пустой пользователь
        );

        // Act
        List<BulkQrResult> results = qrService.generateBulkQrCodes(requests);

        // Assert
        assertEquals(2, results.size());
        assertNull(results.get(0).getQrCodeBase64()); // Первый запрос неуспешен
        assertNotNull(results.get(0).getError());     // Должна быть ошибка
        assertNull(results.get(1).getQrCodeBase64()); // Второй запрос неуспешен
        assertNotNull(results.get(1).getError());
    }
    @Test
    void generateBulkQrCodes_WhenExceptionThrown_ShouldReturnsFailureResult() {
        // Arrange
        List<QrGenerationRequest> requests = Arrays.asList(
                new QrGenerationRequest("text", "testUser")
        );
        when(userRepository.findByUsername("testUser")).thenThrow(new RuntimeException("DB error"));

        // Act
        List<BulkQrResult> results = qrService.generateBulkQrCodes(requests);

        // Assert
        assertEquals(1, results.size());
        assertNull(results.get(0).getQrCodeBase64());
        assertTrue(results.get(0).getError().contains("Processing failed"));
    }
    // Тест на успешное создание QR-кода
    @Test
    void generateAndSaveQrCode_WhenNewUser_ShouldCreateUserAndQr() {
        when(userRepository.findByUsername("newUser")).thenReturn(Optional.empty());
        when(qrDao.generateQrCode(anyString())).thenReturn(new byte[100]);

        QrResponse response = qrService.generateAndSaveQrCode("text", "newUser");

        verify(userRepository).save(any(User.class));
        assertNotNull(response.getQrCode());
    }

    // Тест на исключение при генерации QR
    @Test
    void generateAndSaveQrCode_WhenQrGenerationFails_ShouldThrowException() {
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(qrDao.generateQrCode(anyString())).thenThrow(new RuntimeException("Generation error"));

        assertThrows(RuntimeException.class, () ->
                qrService.generateAndSaveQrCode("text", "user"));
    }
    @Test
    void generateBulkQrCodes_WhenPartialFailures_ShouldReturnMixedResults() {
        // Arrange
        QrGenerationRequest validRequest = new QrGenerationRequest("valid", "user");
        QrGenerationRequest invalidRequest = new QrGenerationRequest("", null);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(qrDao.generateQrCode("valid")).thenReturn(new byte[100]);

        // Act
        List<BulkQrResult> results = qrService.generateBulkQrCodes(
                List.of(validRequest, invalidRequest)
        );

        // Assert
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.getError() == null));
        assertTrue(results.stream().anyMatch(r -> r.getError() != null));
    }

    @Test
    void updateQr_WhenValidInput_ShouldUpdateCache() {
        // Arrange
        QrEntity existingQr = new QrEntity();
        existingQr.setId(1L);
        existingQr.setContent("old");

        when(qrRepository.findById(1L)).thenReturn(Optional.of(existingQr));
        when(qrDao.generateQrCode("new")).thenReturn(new byte[200]);

        // Act
        QrEntity updated = qrService.updateQr(1L, "new");

        // Assert
        verify(cacheService).put(eq("qr_1"), any(QrEntity.class));
        assertEquals("new", updated.getContent());
    }

    @Test
    void getQrCodesByUsername_WhenEmptyResult_ShouldCacheEmptyList() {
        // Arrange
        String username = "emptyUser";
        when(qrRepository.findByUsername(username)).thenReturn(Collections.emptyList());

        // Act
        List<QrEntity> result = qrService.getQrCodesByUsername(username);

        // Assert
        verify(cacheService).put(eq("qr_user_emptyUser"), eq(Collections.emptyList()));
        assertTrue(result.isEmpty());
    }
    @Test
    void deleteQr_WhenQrNotFound_ShouldThrowException() {
        when(qrRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                qrService.deleteQr(999L));
    }

    @Test
    void getQrCodesByUsername_WhenDatabaseError_ShouldHandleGracefully() {
        when(qrRepository.findByUsername("user")).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () ->
                qrService.getQrCodesByUsername("user"));
    }
}