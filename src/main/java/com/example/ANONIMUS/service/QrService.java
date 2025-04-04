package com.example.ANONIMUS.service;

import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.model.QrEntity;
import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.QrRepository;
import com.example.ANONIMUS.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
@Slf4j
public class QrService {
    private final QrDao qrDao;
    private final QrRepository qrRepository;
    private final UserRepository userRepository;

    public QrService(QrDao qrDao, QrRepository qrRepository, UserRepository userRepository) {
        this.qrDao = qrDao;
        this.qrRepository = qrRepository;
        this.userRepository = userRepository;
    }

    public QrResponse generateAndSaveQrCode(String text, String username) {
        try {
            // Найти или создать пользователя
            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(username);
                        return userRepository.save(newUser);
                    });

            // Создать и сохранить QR-код
            QrEntity qrEntity = new QrEntity();
            qrEntity.setContent(text);
            byte[] qrCodeBytes = qrDao.generateQrCode(text);
            qrEntity.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));

            // Сохранить QR-код в БД (получить id)
            qrEntity = qrRepository.save(qrEntity); // Важно: сохранить перед связыванием!

            // Установить связь
            user.getQrCodes().add(qrEntity);
            userRepository.save(user); // Обновить пользователя

            return new QrResponse(qrEntity.getQrCodeBase64());
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new QrGenerationException("Failed to generate QR", e);
        }
    }

}