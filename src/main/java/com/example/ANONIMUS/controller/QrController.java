package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.model.QrResponse;
import com.example.ANONIMUS.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping
    public ResponseEntity<QrResponse> generateQrCode(
            @RequestParam String text,
            @RequestParam String username) {

        if (text == null || text.trim().isEmpty() || username == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(qrService.generateAndSaveQrCode(text, username));
    }
}