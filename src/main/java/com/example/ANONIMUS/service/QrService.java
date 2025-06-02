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
@Transactional // Applied at class level, covers all public methods
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

    // Single QR generation remains the same
    public QrResponse generateAndSaveQrCode(String text, String username) {
        if (text == null || text.trim().isEmpty() || username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Text and username must not be empty");
        }
        User user = findOrCreateUser(username);
        QrEntity qrEntity = createAndSaveQrEntity(text, user);
        return new QrResponse(qrEntity.getQrCodeBase64());
    }

    // Bulk QR generation using Stream API
    public List<BulkQrResult> generateBulkQrCodes(List<QrGenerationRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return Collections.emptyList();
        }

        // Consider potential optimizations here for fetching/creating users if performance is critical
        // For simplicity, we process each item individually within the stream
        return requests.stream()
                .map(request -> {
                    try {
                        // Input validation should ideally happen before calling the service,
                        // but double-check just in case or handle potential nulls
                        if (request.getText() == null || request.getText().trim().isEmpty() ||
                                request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                            return BulkQrResult.failure(request.getText(), request.getUsername(), "Invalid input text or username");
                        }

                        User user = findOrCreateUser(request.getUsername());
                        QrEntity qrEntity = createAndSaveQrEntity(request.getText(), user);
                        // Invalidate cache for this user as their QR list has changed
                        cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername());
                        return BulkQrResult.success(request.getText(), request.getUsername(), qrEntity.getQrCodeBase64());
                    } catch (Exception e) {
                        // Log the exception properly here if needed using logger
                        // log.error("Failed to process QR request for user {}: {}", request.getUsername(), e.getMessage());
                        // Decide on error reporting strategy
                        return BulkQrResult.failure(request.getText(), request.getUsername(), "Processing failed: " + e.getMessage());
                    }
                })
                .collect(Collectors.toList());
        // The @Transactional annotation on the class ensures this whole method runs in a single transaction.
        // If one item fails with an unhandled RuntimeException, the whole transaction *should* roll back.
        // The try-catch block currently prevents rollback for handled exceptions, returning an error marker instead.
    }


    private User findOrCreateUser(String username) {
        return userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setUsername(username);
                    // Invalidate user cache if a new user is created?
                    // Depends on whether user list caching exists or is needed.
                    return userRepository.save(newUser);
                });
    }

    private QrEntity createAndSaveQrEntity(String text, User user) {
        QrEntity qrEntity = new QrEntity();
        qrEntity.setContent(text);
        byte[] qrCodeBytes = qrDao.generateQrCode(text);
        qrEntity.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));

        // Save the QR entity first to get an ID (if needed, though relationship handles it)
        qrEntity = qrRepository.save(qrEntity);

        // Associate with user and save user to update relationship
        user.getQrCodes().add(qrEntity);
        userRepository.save(user); // This cascades the relationship persistence

        // Invalidate specific QR cache? Unlikely needed immediately after creation.
        // Invalidate user's QR list cache
        cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername());

        return qrEntity;
    }


    public QrEntity updateQr(Long id, String newContent) {
        QrEntity qr = qrRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("QR not found with id: " + id));
        qr.setContent(newContent);

        // Generate new QR code based on new content? Assume yes.
        byte[] qrCodeBytes = qrDao.generateQrCode(newContent);
        qr.setQrCodeBase64(Base64.getEncoder().encodeToString(qrCodeBytes));

        QrEntity updatedQr = qrRepository.save(qr);

        // Update cache
        cacheService.put(QR_CACHE_PREFIX + id, updatedQr);
        // Invalidate caches for all users associated with this QR? This can be complex.
        // A simpler approach is to just invalidate the specific QR cache entry.
        // Fetching by user will retrieve the updated entity next time.

        return updatedQr;
    }

    public void deleteQr(Long id) {
        // Need to handle relationships carefully or let cascade deletes do the work if configured.
        // Fetching first might be safer to manage related caches/entities if needed.
        QrEntity qrToDelete = qrRepository.findById(id).orElse(null);
        if (qrToDelete != null) {
            // Manually remove associations to trigger user cache invalidation if necessary
            qrToDelete.getUsers().forEach(user -> {
                user.getQrCodes().remove(qrToDelete);
                userRepository.save(user); // Update user side of relationship
                cacheService.evict(QR_USER_CACHE_PREFIX + user.getUsername()); // Invalidate user's list cache
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
                // log.warn("Cache get failed for key {}: {}", cacheKey, e.getMessage());
                cacheService.evict(cacheKey); // Evict potentially corrupted entry
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
                // Ensure the cache returns the correct type
                List<QrEntity> cachedList = cacheService.getList(cacheKey, QrEntity.class);
                if (cachedList != null) { // Check if cache returned null
                    return cachedList;
                } else {
                    // log.warn("Cache returned null for key {}", cacheKey);
                    cacheService.evict(cacheKey); // Evict null entry
                }
            } catch(ClassCastException e) {
                // log.error("Cache type mismatch for key {}: {}", cacheKey, e.getMessage());
                cacheService.evict(cacheKey); // Evict bad entry
            } catch (Exception e) {
                // log.warn("Cache getList failed for key {}: {}", cacheKey, e.getMessage());
                cacheService.evict(cacheKey); // Evict potentially corrupted entry
            }
        }
        List<QrEntity> qrCodes = qrRepository.findByUsername(username);
        cacheService.put(cacheKey, qrCodes); // Cache the possibly empty list
        return qrCodes;
    }
}