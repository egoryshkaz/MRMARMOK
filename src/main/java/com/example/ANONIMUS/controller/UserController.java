package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.User;
import com.example.ANONIMUS.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Остальные CRUD методы
}