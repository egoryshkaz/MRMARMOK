package com.example.ANONIMUS.service;

import com.example.ANONIMUS.dao.QrDao;
import com.example.ANONIMUS.dto.BulkQrResult;
import com.example.ANONIMUS.dto.QrGenerationRequest;
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class QrService {
    private static final String QR_CACHE_PREFIX = "qr_";
    private static final String QR_USER_CACHE_PREFIX = "qr_user_";


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
        if (text == null || text.trim().isEmpty() || username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Text and username must not be empty");
        }
        User user = findOrCreateUser(username);
        QrEntity qrEntity = createAndSaveQrEntity(text, user);
        return new QrResponse(qrEntity.getQrCodeBase64());
    }


    public List<BulkQrResult> generateBulkQrCodes(List<QrGenerationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }


        return requests.stream()
                .map(request -> {
                    try {

                        if (request.getText() == null || request.getText().trim().isEmpty() ||
                                request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                            return BulkQrResult.failure(request.getText(), request.getUsername(), "Invalid input text or username");
                        }

                        User user = findOrCreateUser(request.getUsername());
                        QrEntity qrEntity = createAndSaveQrEntity(request.getText(), user);

                        cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername());
                        return BulkQrResult.success(request.getText(), request.getUsername(), qrEntity.getQrCodeBase64());
                    } catch (Exception e) {

                        return BulkQrResult.failure(request.getText(), request.getUsername(), "Processing failed: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());

    }


    private User findOrCreateUser(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);

                    return userRepository.save(newUser);
                });
    }

    private QrEntity createAndSaveQrEntity(String text, User user) {
        QrEntity qrEntity = new QrEntity();
        qrEntity.setContent(text);
        byte[] qrCodeBytes = qrDao.generateQrCode(text);
        qrEntity.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));

        qrEntity = qrRepository.save(qrEntity);

        user.getQrCodes().add(qrEntity);
        userRepository.save(user);

        cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername());

        return qrEntity;
    }


    public QrEntity updateQr(Long id, String newContent) {
        QrEntity qr = qrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR not found with id: " + id));
        qr.setContent(newContent);

        byte[] qrCodeBytes = qrDao.generateQrCode(newContent);
        qr.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));

        QrEntity updatedQr = qrRepository.save(qr);


        cacheService.put(QR_CACHE_PREFIX + id, updatedQr);


        return updatedQr;
    }

    public void deleteQr(Long id) {

        QrEntity qrToDelete = qrRepository.findById(id).orElse(null);
        if (qrToDelete != null) {

            qrToDelete.getUsers().forEach(user -> {
                user.getQrCodes().remove(qrToDelete);
                userRepository.save(user);
                cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername());
            });
            qrRepository.deleteById(id);
            cacheService.evict(QR_CACHE_PREFIX + id);
        } else {
            throw new RuntimeException("QR not found with id: " + id);
        }

    }

    public QrEntity getQrById(Long id) {
        String cacheKey = QR_CACHE_PREFIX + id;
        if (cacheService.containsKey(cacheKey)) {
            try {
                return cacheService.get(cacheKey, QrEntity.class);
            } catch (Exception e) {

                cacheService.evict(cacheKey);
            }

        }
        QrEntity qr = qrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR not found with id: " + id));
        cacheService.put(cacheKey, qr);
        return qr;
    }

    public List<QrEntity> getQrCodesByUsername(String username) {
        String cacheKey = QR_USER_CACHE_PREFIX + username;
        if (cacheService.containsKey(cacheKey)) {
            try {

                List<QrEntity> cachedList = cacheService.getList(cacheKey, QrEntity.class);
                if (cachedList != null) {
                    return cachedList;
                } else {

                    cacheService.evict(cacheKey);
                }
            } catch(ClassCastException e) {

                cacheService.evict(cacheKey);
            } catch (Exception e) {

                cacheService.evict(cacheKey);
            }
        }
        List<QrEntity> qrCodes = qrRepository.findByUsername(username);
        cacheService.put(cacheKey, qrCodes);
        return qrCodes;
    }
}