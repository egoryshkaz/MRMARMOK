package com.example.ANONIMUS.service;

import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.exceptions.QrGenerationException;
import com.example.ANONIMUS.model.QrEntity;
import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.QrRepository;
import com.example.ANONIMUS.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.Base64;
import com.example.ANONIMUS.cache.CacheService;
import java.util.Optional;
import java.util.List;

@Service
@Transactional
@Slf4j
public class QrService {
    private static final String QR_CACHE_PREFIX = "qr_";

    private final QrDao qrDao;
    private final QrRepository qrRepository;
    private final UserRepository userRepository;
    private final CacheService cacheService;

    public QrService(QrDao qrDao, QrRepository qrRepository,
                     UserRepository userRepository, CacheService cacheService) {
        this.qrDao = qrDao;
        this.qrRepository = qrRepository;
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public QrResponse generateAndSaveQrCode(String text, String username) {
        try {

            User user = userRepository.findByUsername(username)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setUsername(username);
                        return userRepository.save(newUser);
                    });


            QrEntity qrEntity = new QrEntity();
            qrEntity.setContent(text);
            byte[] qrCodeBytes = qrDao.generateQrCode(text);
            qrEntity.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));


            qrEntity = qrRepository.save(qrEntity);


            user.getQrCodes().add(qrEntity);
            userRepository.save(user);

            return new QrResponse(qrEntity.getQrCodeBase64());
        } catch (Exception e) {
            log.error("Error generating QR code", e);
            throw new QrGenerationException("Failed to generate QR", e);
        }
    }
    public QrEntity updateQr(Long id, String newContent) {
        QrEntity qr = qrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR not found"));
        qr.setContent(newContent);

        return qrRepository.save(qr);
    }

    public void deleteQr(Long id) {
        qrRepository.deleteById(id);
        cacheService.evict(QR_CACHE_PREFIX + id);
    }
    public QrEntity getQrById(Long id) {
        String cacheKey = QR_CACHE_PREFIX + id;
        if (cacheService.containsKey(cacheKey)) {
            return cacheService.get(cacheKey, QrEntity.class);
        }
        QrEntity qr = qrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR not found"));
        cacheService.put(cacheKey, qr);
        return qr;
    }
    public List<QrEntity> getQrCodesByUsername(String username) {
        String cacheKey = "qr_user_" + username;
        if (cacheService.containsKey(cacheKey)) {
            return cacheService.getList(cacheKey, QrEntity.class);
        }
        List<QrEntity> qrCodes = qrRepository.findByUsername(username);
        cacheService.put(cacheKey, qrCodes);
        return qrCodes;
    }
}