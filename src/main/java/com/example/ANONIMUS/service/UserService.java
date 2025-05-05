package com.example.ANONIMUS.service;

import com.example.ANONIMUS.cache.CacheService;
import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private static final String USER_CACHE_PREFIX = "user_";

    private final UserRepository userRepository;
    private final CacheService cacheService;

    public UserService(UserRepository userRepository, CacheService cacheService) {
        this.userRepository = userRepository;
        this.cacheService = cacheService;
    }

    public User createUser(User user) {
        User savedUser = userRepository.save(user);
        cacheService.put(USER_CACHE_PREFIX + savedUser.getId(), savedUser);
        return savedUser;
    }

    public Optional<User> getUserById(Long id) {
        String cacheKey = USER_CACHE_PREFIX + id;
        if (cacheService.containsKey(cacheKey)) {
            return Optional.of(cacheService.get(cacheKey, User.class));
        }
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> cacheService.put(cacheKey, u));
        return user;
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setUsername(userDetails.getUsername());
                    User updatedUser = userRepository.save(user);
                    cacheService.put(USER_CACHE_PREFIX + id, updatedUser);
                    return updatedUser;
                })
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        cacheService.evict(USER_CACHE_PREFIX + id);
    }
}