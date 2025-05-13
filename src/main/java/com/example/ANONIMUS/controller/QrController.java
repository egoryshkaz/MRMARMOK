package com.example.ANONIMUS.controller;

import com.example.ANONIMUS.dto.QrResponse;
import com.example.ANONIMUS.model.QrEntity; // Добавлен импорт
import com.example.ANONIMUS.service.QrService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/api/qr")
public class QrController {

    private final QrService qrService;

    public QrController(QrService qrService) {
        this.qrService = qrService;
    }

    @GetMapping
    @Operation(summary = "Generate QR code", description = "Creates QR code from text")
    @ApiResponse(responseCode = "200", description = "QR generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    public ResponseEntity<QrResponse> generateQrCode(
            @RequestParam String text,
            @RequestParam String username) {

        if (text == null || text.trim().isEmpty() || username == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(qrService.generateAndSaveQrCode(text, username));
    }
    @GetMapping("/by-user")
    public ResponseEntity<List<QrEntity>> getQrCodesByUser(
            @RequestParam String username) {

        List<QrEntity> qrCodes = qrService.getQrCodesByUsername(username);
        return ResponseEntity.ok(qrCodes);
    }
}